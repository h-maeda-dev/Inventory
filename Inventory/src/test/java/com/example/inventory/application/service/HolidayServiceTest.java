// src/test/java/com/example/inventory/application/service/HolidayServiceTest.java
package com.example.inventory.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.inventory.domain.model.Holiday;
import com.example.inventory.domain.repository.HolidayRepository;

@ExtendWith(MockitoExtension.class)
public class HolidayServiceTest {

    @Mock private HolidayRepository holidayRepository;

    @InjectMocks
    private HolidayService holidayService;

    @Test
    void 全ての休業日が取得できること() {
        Holiday h1 = new Holiday(); h1.setHolidayDate(LocalDate.of(2026, 1, 1));
        Holiday h2 = new Holiday(); h2.setHolidayDate(LocalDate.of(2026, 5, 3));
        when(holidayRepository.findAll()).thenReturn(Arrays.asList(h1, h2));

        List<Holiday> result = holidayService.getAllHolidays();
        assertEquals(2, result.size());
    }

    @Test
    void 休業日の追加がリポジトリに正しく渡されること() {
        LocalDate targetDate = LocalDate.of(2026, 12, 25);
        holidayService.addHoliday(targetDate, "クリスマス休業");

        verify(holidayRepository, times(1)).save(argThat(h -> 
            h.getHolidayDate().equals(targetDate) && h.getHolidayName().equals("クリスマス休業")
        ));
    }

    @Test
    void 営業日計算が正しく行われること_週末と祝日をスキップする() {
        // 2026年11月2日(月) を基準日とする
        LocalDate targetDate = LocalDate.of(2026, 11, 2);
        
        // 11月3日(火)を祝日としてモック設定
        when(holidayRepository.existsById(LocalDate.of(2026, 11, 3))).thenReturn(true);
        // 土日はデフォルトでスキップ対象（10/31, 11/1）
        when(holidayRepository.existsById(any(LocalDate.class))).thenReturn(false);

        // Act: 11/2(月)から3営業日前を計算 -> 11/1(日), 10/31(土)を飛ばし、10/30(金), 10/29(木), 10/28(水) となる
        LocalDate result = holidayService.calculateBusinessDaysBefore(targetDate, 3);

        // Assert
        assertEquals(LocalDate.of(2026, 10, 28), result);
    }
}