package com.notepad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminController {

    // ============================================================
    //  📌 配置区：修改这里的密码
    // ============================================================
    private static final String ADMIN_PASSWORD = "1234";

    // 图片存储目录
    private static final String IMAGE_DIR = "src/main/resources/static/images/";

    // ============================================================
    //  密码验证
    // ============================================================

    @GetMapping("/")
    public String index() {
        return "login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        if (ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("isAdmin", true);
            return "redirect:/gallery";
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ 密码错误");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ============================================================
    //  图片展示（普通用户看到的）
    // ============================================================

    @GetMapping("/gallery")
    public String gallery(HttpSession session, Model model) {
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/login";
        }

        List<String> images = getImageList();
        model.addAttribute("images", images);
        return "gallery";
    }

    // ============================================================
    //  图片管理（管理员后台）
    // ============================================================

    @GetMapping("/admin")
    public String adminPage(HttpSession session, Model model) {
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/login";
        }

        List<String> images = getImageList();
        model.addAttribute("images", images);
        return "admin";
    }

    /**
     * 上传新图片
     */
    @PostMapping("/admin/upload")
    public String uploadImage(@RequestParam("image") MultipartFile file,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/login";
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "❌ 请选择图片");
            return "redirect:/admin";
        }

        try {
            File dir = new File(IMAGE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保留原文件名（管理员自己设计的图片，方便识别）
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) {
                originalName = "image_" + System.currentTimeMillis() + ".jpg";
            }

            // 如果文件名已存在，加时间戳
            File targetFile = new File(IMAGE_DIR + originalName);
            if (targetFile.exists()) {
                String name = originalName.substring(0, originalName.lastIndexOf("."));
                String ext = originalName.substring(originalName.lastIndexOf("."));
                originalName = name + "_" + System.currentTimeMillis() + ext;
            }

            Path filePath = Paths.get(IMAGE_DIR + originalName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            redirectAttributes.addFlashAttribute("success", "✅ 图片上传成功！");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "❌ 上传失败：" + e.getMessage());
        }

        return "redirect:/admin";
    }

    /**
     * 删除图片
     */
    @GetMapping("/admin/delete/{filename}")
    public String deleteImage(@PathVariable String filename,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/login";
        }

        File file = new File(IMAGE_DIR + filename);
        if (file.exists()) {
            if (file.delete()) {
                redirectAttributes.addFlashAttribute("success", "🗑️ 图片已删除");
            } else {
                redirectAttributes.addFlashAttribute("error", "❌ 删除失败");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ 图片不存在");
        }

        return "redirect:/admin";
    }

    /**
     * 获取图片列表
     */
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
}