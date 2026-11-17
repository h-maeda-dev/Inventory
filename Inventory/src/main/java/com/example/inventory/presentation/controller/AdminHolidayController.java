// src/main/java/com/example/inventory/presentation/controller/AdminHolidayController.java
package com.example.inventory.presentation.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.inventory.application.service.HolidayService;
import com.example.inventory.domain.model.Holiday;

import lombok.RequiredArgsConstructor;

/**
 * 管理者向けの休業日マスタ管理コントローラー。
 * 個別の休業日追加や削除、およびCSVファイルからの祝日一括インポートを処理します。
 */
@Controller
@RequestMapping("/admin/holidays")
@RequiredArgsConstructor
public class AdminHolidayController {
    private final HolidayService holidayService;

    /**
     * 登録済みの休業日一覧と追加・インポート用のフォームを表示します。
     *
     * @param model ビューへ渡すデータモデル
     * @return 休業日マスタ管理画面のテンプレート名
     */
    @GetMapping
    public String listHolidays(Model model) {
        model.addAttribute("holidays", holidayService.getAllHolidays());
        return "admin_holidays";
    }

    /**
     * 個別の休業日を手動で追加・上書きします。
     *
     * @param holidayDate 追加する休業日の日付
     * @param holidayName 休業日の名称
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 一覧画面へのリダイレクト
     */
    @PostMapping("/add")
    public String addHoliday(
            @RequestParam("holidayDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate holidayDate,
            @RequestParam("holidayName") String holidayName,
            RedirectAttributes redirectAttributes) {
        
        holidayService.addHoliday(holidayDate, holidayName);
        redirectAttributes.addFlashAttribute("successMessage", "休業日「" + holidayName + "」を追加（上書き）しました");
        
        return "redirect:/admin/holidays";
    }

    /**
     * 指定された休業日を削除します。
     *
     * @param date 削除対象の日付
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 一覧画面へのリダイレクト
     */
    @PostMapping("/{date}/delete")
    public String deleteHoliday(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, 
            RedirectAttributes redirectAttributes) {
        
        // メッセージ表示用に削除前に休業日名を取得
        String hName = holidayService.getAllHolidays().stream()
                .filter(h -> h.getHolidayDate().equals(date))
                .findFirst().map(Holiday::getHolidayName).orElse("不明な休業日");

        holidayService.deleteHoliday(date);
        redirectAttributes.addFlashAttribute("successMessage", "休業日「" + hName + "」を削除しました");
        
        return "redirect:/admin/holidays";
    }

    /**
     * 祝日CSVファイル（内閣府提供等のフォーマット）をインポートします。
     *
     * @param file アップロードされたCSVファイル
     * @param redirectAttributes リダイレクト先へのフラッシュメッセージ
     * @return 一覧画面へのリダイレクト
     */
    @PostMapping("/import")
    public String importCsv(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "ファイルが選択されていません");
            return "redirect:/admin/holidays";
        }
        
        try {
            holidayService.importCsv(file);
            redirectAttributes.addFlashAttribute("successMessage", "祝日データのインポートが完了しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "インポートに失敗しました: " + e.getMessage());
        }
        
        return "redirect:/admin/holidays";
    }
}