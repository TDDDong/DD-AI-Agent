package com.dd.ddaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.dd.ddaiagent.constant.FileConstant;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class PDFGenerationTool {

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 自定义字体（需要人工下载字体文件到特定目录）
//                String fontPath = Paths.get("src/main/resources/static/fonts/simsun.ttf")
//                        .toAbsolutePath().toString();
//                PdfFont font = PdfFontFactory.createFont(fontPath,
//                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                // 使用内置中文字体
                // 设置中文字体 - 使用最基本的方式
                PdfFont regularFont = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");

                // 处理内容
                processContent(content, document, pdf, regularFont);

                // 创建段落
                Paragraph paragraph = new Paragraph(content);
                // 添加段落并关闭文档
                document.add(paragraph);
            }
            return "PDF generated successfully to: " + filePath;
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }

    private void processContent(String content, Document document, PdfDocument pdf, PdfFont font) throws IOException {
        // 分割内容为行
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 处理标题
            if (line.startsWith("# ")) {
                String titleText = line.substring(2);
                Paragraph title = new Paragraph(titleText);
                title.setFont(font);
                title.setFontSize(20);
                title.setTextAlignment(TextAlignment.CENTER);
                title.setMarginBottom(15);
                title.setMarginTop(10);
                document.add(title);
                continue;
            }

            if (line.startsWith("## ")) {
                String subtitleText = line.substring(3);
                Paragraph subtitle = new Paragraph(subtitleText);
                subtitle.setFont(font);
                subtitle.setFontSize(16);
                subtitle.setMarginBottom(10);
                subtitle.setMarginTop(10);
                document.add(subtitle);
                continue;
            }

            if (line.startsWith("### ")) {
                String subsubtitleText = line.substring(4);
                Paragraph subsubtitle = new Paragraph(subsubtitleText);
                subsubtitle.setFont(font);
                subsubtitle.setFontSize(14);
                subsubtitle.setMarginBottom(5);
                subsubtitle.setMarginTop(5);
                document.add(subsubtitle);
                continue;
            }

            // 处理分隔线
            if (line.startsWith("---")) {
                // 简单使用一个带下划线的空段落作为分隔线
                Paragraph separator = new Paragraph("_".repeat(80));
                separator.setFont(font);
                separator.setFontSize(1);
                separator.setMarginTop(10);
                separator.setMarginBottom(10);
                document.add(separator);
                continue;
            }

            // 处理列表项
            if (line.startsWith("- ") || line.startsWith("* ")) {
                String listItemText = line.substring(2);
                // 简化列表处理，使用普通段落加缩进和前缀
                Paragraph listItem = new Paragraph("• " + listItemText);
                listItem.setFont(font);
                listItem.setFontSize(12);
                listItem.setMarginLeft(20);
                listItem.setMarginBottom(2);
                document.add(listItem);
                continue;
            }

            // 处理图片
            if (line.contains("![") && line.contains("](") && line.contains(")")) {
                // 提取图片URL
                int startIndex = line.indexOf("](") + 2;
                int endIndex = line.indexOf(")", startIndex);
                String imageUrl = line.substring(startIndex, endIndex);

                try {
                    // 从URL加载图片
                    ImageData imageData = ImageDataFactory.create(new URL(imageUrl));
                    Image image = new Image(imageData);

                    // 设置图片宽度为页面宽度的80%
                    float pageWidth = pdf.getDefaultPageSize().getWidth() - document.getLeftMargin() - document.getRightMargin();
                    image.setWidth(pageWidth * 0.8f);
                    image.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    image.setMarginBottom(10);
                    image.setMarginTop(10);
                    document.add(image);
                } catch (Exception e) {
                    Paragraph errorPara = new Paragraph("图片加载失败: " + imageUrl);
                    errorPara.setFont(font);
                    errorPara.setFontSize(10);
                    document.add(errorPara);
                }
                continue;
            }

            // 如果是空行，添加一个小间距
            if (line.trim().isEmpty()) {
                Paragraph emptyLine = new Paragraph(" ");
                emptyLine.setMarginBottom(5);
                document.add(emptyLine);
                continue;
            }

            // 简化处理加粗文本 - 不尝试特殊格式化，直接替换掉**标记
            if (!line.isEmpty()) {
                // 将**text**替换为text
                String processedLine = line.replaceAll("\\*\\*(.*?)\\*\\*", "$1");

                Paragraph paragraph = new Paragraph(processedLine);
                paragraph.setFont(font);
                paragraph.setFontSize(12);
                paragraph.setMarginBottom(5);
                document.add(paragraph);
            }
        }
    }
}
