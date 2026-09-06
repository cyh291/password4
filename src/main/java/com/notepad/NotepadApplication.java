package com.notepad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotepadApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotepadApplication.class, args);
        System.out.println("✅ 图片展示系统已启动！");
        System.out.println("🌐 http://localhost:8080");
    }
}