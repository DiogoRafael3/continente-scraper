package com.example.demo.service;

import com.example.demo.model.Product;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ScrapingService {

    private static final String[] divisions = { //Subject to changes on the website's part
            "refeicoes-faceis"
    };

    public void scrapeProductData() {
        for (String division : divisions) {
            String url = "https://www.continente.pt/" + division + "/";
            List<Product> products = new ArrayList<>();

            System.setProperty("webdriver.gecko.driver", "D:\\scraper\\geckodriver.exe");

            FirefoxOptions options = new FirefoxOptions();
            options.setBinary("C:\\Program Files\\Mozilla Firefox\\firefox.exe");

            WebDriver driver = new FirefoxDriver(options);

            try {
                driver.get(url);
                driver.manage().timeouts().implicitlyWait(Duration.of(10, TimeUnit.SECONDS.toChronoUnit()));

                // Attempt to click the "Personalizar" button
                try {
                    WebElement customizeButton = driver.findElement(By.id("CybotCookiebotDialogBodyLevelButtonCustomize"));
                    customizeButton.click();
                    System.out.println("Clicked on 'Personalizar' button.");
                } catch (Exception e) {
                    System.out.println("'Personalizar' button not found or already clicked.");
                }

                // Attempt to click the "Rejeitar todos" button
                try {
                    WebElement declineButton = driver.findElement(By.id("CybotCookiebotDialogBodyButtonDecline"));
                    declineButton.click();
                    System.out.println("Clicked on 'Rejeitar todos' button.");
                } catch (Exception e) {
                    System.out.println("'Rejeitar todos' button not found or already clicked.");
                }

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(5); // Make sure the program waits for the "See More" button to show up
                        WebElement seeMoreButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("js-show-more-products")));
                        seeMoreButton.click();
                        System.out.println("Clicking 'See More' button.");
                        TimeUnit.SECONDS.sleep(3 + (int) (Math.random() * 5)); // Prevents pattern detection, to be honest
                    } catch (Exception e) {
                        System.out.println("No 'See More' button.");
                        break;
                    }
                }

                String pageSource = driver.getPageSource();
                Document document = Jsoup.parse(pageSource);
                Elements productElements = document.select("div.product");

                int rowCount = 0; // Track the current row for appending in the Excel file

                for (Element productElement : productElements) {
                    // Extract the product details
                    String productName = productElement.select(".pwc-tile--description").text();
                    String productPrice = productElement.select("div.prices-wrapper .value .ct-price-formatted").text();
                    String productUnit = productElement.select("div.prices-wrapper .pwc-m-unit").text();
                    String fullPriceString = productPrice + " " + productUnit;
                    String productPid = productElement.attr("data-pid");
                    String productQty = productElement.select(".pwc-tile--quantity").text();

                    // Add product to the list
                    products.add(new Product(productPid, productName, fullPriceString, productQty));

                    if (products.size() >= 1000) {
                        rowCount = writeProductsToExcel(products, rowCount, division);
                        products.clear(); // Clear the list after writing the batch
                    }
                }

                // Write any remaining products after the loop
                if (!products.isEmpty()) {
                    writeProductsToExcel(products, rowCount, division);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                driver.close();
            }
        }
    }

    public int writeProductsToExcel(List<Product> products, int startRow, String section) {
        Workbook workbook;
        Sheet sheet;
        FileOutputStream fileOut = null;

        if (section.contains("/")) {
            section = section.split("/")[0];
        }
        String filePath = "products-" + section + ".xlsx";

        try (FileInputStream fileIn = new FileInputStream(filePath)) {
            workbook = new XSSFWorkbook(fileIn);
            sheet = workbook.getSheetAt(0);
        } catch (IOException e) {
            // If the file doesn't exist, create a new one
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Products");

            // Create header row if the file is new
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Product Name");
            headerRow.createCell(1).setCellValue("Price");
            headerRow.createCell(2).setCellValue("PID");
            headerRow.createCell(3).setCellValue("Quantity");

            startRow = 1;
        }

        // Populate data rows
        for (Product product : products) {
            Row row = sheet.createRow(startRow++);
            row.createCell(0).setCellValue(product.getName());
            row.createCell(1).setCellValue(product.getPrice());
            row.createCell(2).setCellValue(product.getPid());
            row.createCell(3).setCellValue(product.getQty());
        }

        // Auto-size columns for better visibility
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to the output file
        try {
            fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return startRow;
    }
}
