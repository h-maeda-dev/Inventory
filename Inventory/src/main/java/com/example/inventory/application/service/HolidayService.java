// src/main/java/com/example/inventory/application/service/HolidayService.java
package com.example.inventory.application.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.inventory.domain.model.Holiday;
import com.example.inventory.domain.repository.HolidayRepository;

import lombok.RequiredArgsConstructor;

/**
 * 休業日マスタの管理、および休業日を考慮した営業日計算を行うサービスクラス。
 */
@Service
@RequiredArgsConstructor
public class HolidayService {
    private final HolidayRepository holidayRepository;

    /**
     * 登録済みの全休業日を取得します。
     *
     * @return 休業日のリスト
     */
    @Transactional(readOnly = true)
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    /**
     * 個別の休業日を手動で追加・上書きします。
     *
     * @param date 休業日の日付
     * @param name 休業日の名称
     */
    @Transactional
    public void addHoliday(LocalDate date, String name) {
        Holiday holiday = new Holiday();
        holiday.setHolidayDate(date);
        holiday.setHolidayName(name);
        holidayRepository.save(holiday);
    }
    
    /**
     * 指定された日付の休業日を削除します。
     *
     * @param date 削除対象の日付
     */
    @Transactional
    public void deleteHoliday(LocalDate date) {
        holidayRepository.deleteById(date);
    }
    
    /**
     * 内閣府等が提供する祝日CSVファイルをインポートし、一括で休業日を登録します。
     *
     * @param file アップロードされたCSVファイル
     * @throws RuntimeException ファイルの読み込みや解析に失敗した場合
     */
    @Transactional
    public void importCsv(MultipartFile file) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), Charset.forName("Shift_JIS")))) {
            String line;
            boolean isFirst = true;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
            
            while ((line = br.readLine()) != null) {
                // 1行目のヘッダをスキップ
                if (isFirst) { 
                    isFirst = false; 
                    continue; 
                }
                
                String[] cols = line.split(",");
                if (cols.length >= 2) {
                    addHoliday(LocalDate.parse(cols[0], formatter), cols[1]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CSVファイルの読み込みに失敗しました", e);
        }
    }

    /**
     * 指定された基準日から、土日と登録された休業日を除外して指定営業日前の日付を計算します。
     * 期日前通知（リマインド）の送信対象日を特定するために使用します。
     *
     * @param targetDate 基準日（例: 返却期限日）
     * @param businessDaysToSubtract 遡る営業日数
     * @return 計算結果の日付
     */
    public LocalDate calculateBusinessDaysBefore(LocalDate targetDate, int businessDaysToSubtract) {
        LocalDate date = targetDate;
        int subtracted = 0;
        
        while (subtracted < businessDaysToSubtract) {
            date = date.minusDays(1);
            if (!isHolidayOrWeekend(date)) {
                subtracted++;
            }
        }
        return date;
    }

    /**
     * 指定された日付が土日、または休業日マスタに登録されているか判定する内部メソッド。
     *
     * @param date 判定する日付
     * @return 土日または休業日であれば true
     */
    private boolean isHolidayOrWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return true;
        }
        return holidayRepository.existsById(date);
    }
}