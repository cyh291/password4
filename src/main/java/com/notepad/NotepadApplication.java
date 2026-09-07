package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@RestController
public class DemoApplication {

    // ===== 配置 =====
    private static final String ADMIN_PASSWORD = "1234";  // 管理员密码
    private static final String IMAGE_DIR = "uploads/";   // 图片存储目录

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        new File(IMAGE_DIR).mkdirs();
        System.out.println("✅ 图片目录已创建: " + IMAGE_DIR);
    }

    // ============================================================
    //  1. 访客页面：展示所有图片
    // ============================================================
    @GetMapping("/")
    public String home() {
        List<String> images = getImageList();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>图片展示</title>");
        html.append("<style>");
        html.append("body{background:#0f172a;color:#fff;font-family:system-ui;padding:30px;text-align:center}");
        html.append(".gallery{display:grid;grid-template-columns:repeat(auto-fill,minmax(250px,1fr));gap:20px;max-width:1000px;margin:20px auto}");
        html.append(".card{background:rgba(255,255,255,0.05);border-radius:16px;overflow:hidden;border:1px solid rgba(255,255,255,0.06)}");
        html.append(".card img{width:100%;aspect-ratio:1/1;object-fit:cover;display:block}");
        html.append(".admin-link{margin-top:20px;display:inline-block;padding:10px 30px;background:#818cf8;color:#fff;border-radius:30px;text-decoration:none}");
        html.append(".empty{color:#64748b;padding:60px 20px;font-size:18px}");
        html.append("</style></head><body>");
        html.append("<h1>📸 图片展示</h1>");

        if (images.isEmpty()) {
            html.append("<div class='empty'>📭 暂无图片</div>");
        } else {
            html.append("<div class='gallery'>");
            for (String img : images) {
                html.append("<div class='card'>");
                html.append("<img src='/images/").append(img).append("' alt='").append(img).append("'>");
                html.append("</div>");
            }
            html.append("</div>");
        }

        html.append("<a href='/admin' class='admin-link'>⚙️ 管理图片</a>");
        html.append("</body></html>");
        return html.toString();
    }

    // ============================================================
    //  2. 管理页面：需要密码验证
    // ============================================================
    @GetMapping("/admin")
    public String admin(HttpSession session) {
        Object loggedIn = session.getAttribute("loggedIn");
        if (loggedIn == null || !(Boolean) loggedIn) {
            return getLoginForm();
        }

        List<String> images = getImageList();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>图片管理</title>");
        html.append("<style>");
        html.append("body{background:#0f172a;color:#fff;font-family:system-ui;padding:30px}");
        html.append(".container{max-width:1000px;margin:0 auto}");
        html.append(".header{display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid rgba(255,255,255,0.06);padding-bottom:20px}");
        html.append(".header a{color:#94a3b8;text-decoration:none;padding:8px 20px;border:1px solid rgba(255,255,255,0.1);border-radius:30px}");
        html.append(".upload-box{background:rgba(255,255,255,0.04);border:2px dashed rgba(255,255,255,0.08);border-radius:20px;padding:30px;margin:30px 0;text-align:center}");
        html.append(".upload-box input[type='file']{padding:12px;background:#1e293b;border:1px solid rgba(255,255,255,0.1);border-radius:30px;color:#fff;margin-right:10px}");
        html.append(".upload-box button{padding:12px 30px;background:#34d399;border:none;border-radius:30px;color:#fff;font-size:16px;cursor:pointer}");
        html.append(".gallery{display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:20px}");
        html.append(".card{background:rgba(255,255,255,0.05);border-radius:16px;overflow:hidden;border:1px solid rgba(255,255,255,0.06)}");
        html.append(".card img{width:100%;aspect-ratio:1/1;object-fit:cover;display:block}");
        html.append(".card .actions{padding:10px;display:flex;justify-content:space-between;align-items:center}");
        html.append(".card .actions span{color:#94a3b8;font-size:12px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;flex:1}");
        html.append(".card .actions a{color:#f87171;text-decoration:none;font-size:13px}");
        html.append(".msg{color:#34d399;margin-top:10px}");
        html.append(".empty{color:#64748b;text-align:center;padding:40px}");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'><h1>⚙️ 图片管理</h1><a href='/'>⬅️ 返回展示</a></div>");

        html.append("<div class='upload-box'>");
        html.append("<form method='post' action='/upload' enctype='multipart/form-data'>");
        html.append("<input type='file' name='image' accept='image/*' required>");
        html.append("<button type='submit'>📤 上传</button>");
        html.append("</form></div>");

        if (images.isEmpty()) {
            html.append("<div class='empty'>📭 还没有图片</div>");
        } else {
            html.append("<div class='gallery'>");
            for (String img : images) {
                html.append("<div class='card'>");
                html.append("<img src='/images/").append(img).append("' alt='").append(img).append("'>");
                html.append("<div class='actions'>");
                html.append("<span>").append(img).append("</span>");
                html.append("<a href='/delete/").append(img).append("' onclick='return confirm(\"确定删除？\")'>删除</a>");
                html.append("</div></div>");
            }
            html.append("</div>");
        }

        html.append("</div></body></html>");
        return html.toString();
    }

    // ============================================================
    //  3. 登录验证
    // ============================================================
    @PostMapping("/login")
    public String login(@RequestParam String password, HttpSession session) {
        if (ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("loggedIn", true);
            return "redirect:/admin";
        } else {
            return getLoginForm("❌ 密码错误，请重试");
        }
    }

    private String getLoginForm() {
        return getLoginForm(null);
    }

    private String getLoginForm(String error) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>管理员登录</title>");
        html.append("<style>");
        html.append("body{background:#0f172a;color:#fff;font-family:system-ui;display:flex;justify-content:center;align-items:center;min-height:100vh}");
        html.append(".box{background:rgba(255,255,255,0.05);border-radius:30px;padding:40px;width:350px;text-align:center}");
        html.append(".box input{width:100%;padding:14px;border-radius:30px;border:1px solid rgba(255,255,255,0.1);background:#1e293b;color:#fff;font-size:16px;margin:10px 0}");
        html.append(".box button{width:100%;padding:14px;background:#818cf8;border:none;border-radius:30px;color:#fff;font-size:18px;cursor:pointer}");
        html.append(".box .error{color:#f87171;font-size:14px;margin:10px 0}");
        html.append("</style></head><body>");
        html.append("<div class='box'>");
        html.append("<h2>🔐 管理员登录</h2>");
        if (error != null) {
            html.append("<div class='error'>").append(error).append("</div>");
        }
        html.append("<form method='post' action='/login'>");
        html.append("<input type='password' name='password' placeholder='请输入管理员密码' autofocus required>");
        html.append("<button type='submit'>登录</button>");
        html.append("</form>");
        html.append("</div></body></html>");
        return html.toString();
    }

    // ============================================================
    //  4. 上传图片
    // ============================================================
    @PostMapping("/upload")
    public String uploadImage(@RequestParam("image") MultipartFile file,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/admin";
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg", "❌ 请选择图片");
            return "redirect:/admin";
        }

        try {
            new File(IMAGE_DIR).mkdirs();
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) {
                originalName = "image_" + System.currentTimeMillis() + ".jpg";
            }

            File targetFile = new File(IMAGE_DIR + originalName);
            if (targetFile.exists()) {
                String name = originalName.substring(0, originalName.lastIndexOf("."));
                String ext = originalName.substring(originalName.lastIndexOf("."));
                originalName = name + "_" + System.currentTimeMillis() + ext;
            }

            Path filePath = Paths.get(IMAGE_DIR + originalName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            redirectAttributes.addFlashAttribute("msg", "✅ 上传成功！");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("msg", "❌ 上传失败");
        }

        return "redirect:/admin";
    }

    // ============================================================
    //  5. 删除图片
    // ============================================================
    @GetMapping("/delete/{filename}")
    public String deleteImage(@PathVariable String filename, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin";
        }

        File file = new File(IMAGE_DIR + filename);
        if (file.exists()) {
            file.delete();
        }
        return "redirect:/admin";
    }

    // ============================================================
    //  6. 查看图片（供前端展示）
    // ============================================================
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(IMAGE_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok().body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================
    //  7. 辅助方法
    // ============================================================
    private List<String> getImageList() {
        List<String> images = new ArrayList<>();
        File dir = new File(IMAGE_DIR);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        images.add(file.getName());
                    }
                }
            }
        }
        return images;
    }

    private boolean isAdmin(HttpSession session) {
        Object loggedIn = session.getAttribute("loggedIn");
        return loggedIn != null && (Boolean) loggedIn;
    }
}