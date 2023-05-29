package com.richrb97.users1.utils;

import com.richrb97.users1.document.User;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.concurrent.atomic.AtomicInteger;

public class ExportToExcel {
    public static byte[] exportUsers(Flux<User> users) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        String[] headers = {"Row Number", "Name", "Age", "Email", "Address", "Role"};

        Row headerRow = sheet.createRow(0);
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerCellStyle.setFont(headerFont);

        Flux.range(0, headers.length)
                .subscribe(columnIndex -> {
                    String header = headers[columnIndex];

                    Cell cell = headerRow.createCell(columnIndex);
                    cell.setCellValue(header);
                    cell.setCellStyle(headerCellStyle);
                });

        AtomicInteger rowNum = new AtomicInteger(1);
        users.subscribe(user -> {
            Row row = sheet.createRow(rowNum.getAndIncrement());
            row.createCell(0).setCellValue(row.getRowNum());
            row.createCell(1).setCellValue(user.getName());
            row.createCell(2).setCellValue(user.getAge());
            row.createCell(3).setCellValue(user.getEmail());
            row.createCell(4).setCellValue(user.getAddress());
            row.createCell(5).setCellValue(user.getRol());
        });

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);

        return outputStream.toByteArray();
    }
}
