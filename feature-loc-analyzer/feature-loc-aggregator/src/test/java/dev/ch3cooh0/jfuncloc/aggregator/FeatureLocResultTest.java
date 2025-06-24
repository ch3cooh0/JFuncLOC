package dev.ch3cooh0.jfuncloc.aggregator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeatureLocResultTest {

    @Test
    void testFeatureLocResultCreation() {
        FeatureLocResult result = new FeatureLocResult(
            "ユーザー管理機能",
            "ユーザーの作成、更新、削除を行う機能",
            3,
            15,
            45,
            850,
            650,
            125
        );
        
        assertEquals("ユーザー管理機能", result.getFeatureName());
        assertEquals("ユーザーの作成、更新、削除を行う機能", result.getFeatureDescription());
        assertEquals(3, result.getEntryPointCount());
        assertEquals(15, result.getTargetClassCount());
        assertEquals(45, result.getTargetFunctionCount());
        assertEquals(850, result.getTotalClassLoc());
        assertEquals(650, result.getTotalFunctionLoc());
        assertEquals(125, result.getCallGraphEdgeCount());
    }

    @Test
    void testToCsvRow() {
        FeatureLocResult result = new FeatureLocResult(
            "テスト機能",
            "テスト用機能",
            2,
            10,
            25,
            500,
            300,
            50
        );
        
        String[] csvRow = result.toCsvRow();
        
        assertEquals(8, csvRow.length);
        assertEquals("テスト機能", csvRow[0]);
        assertEquals("テスト用機能", csvRow[1]);
        assertEquals("2", csvRow[2]);
        assertEquals("10", csvRow[3]);
        assertEquals("25", csvRow[4]);
        assertEquals("500", csvRow[5]);
        assertEquals("300", csvRow[6]);
        assertEquals("50", csvRow[7]);
    }

    @Test
    void testGetCsvHeader() {
        String[] headers = FeatureLocResult.getCsvHeader();
        
        assertEquals(8, headers.length);
        assertEquals("機能名", headers[0]);
        assertEquals("機能説明", headers[1]);
        assertEquals("エントリーポイント数", headers[2]);
        assertEquals("対象クラス数", headers[3]);
        assertEquals("対象関数数", headers[4]);
        assertEquals("クラス総LOC", headers[5]);
        assertEquals("関数総LOC", headers[6]);
        assertEquals("コールグラフエッジ数", headers[7]);
    }

    @Test
    void testToCsvRowWithNullDescription() {
        FeatureLocResult result = new FeatureLocResult(
            "機能A",
            null,
            1,
            5,
            10,
            100,
            80,
            20
        );
        
        String[] csvRow = result.toCsvRow();
        assertEquals("", csvRow[1]);
    }
}