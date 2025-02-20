# Scraping Service API
## Overview

The Scraping Service is a REST API that scrapes product data from Continente.pt and stores it in an Excel file. It navigates through different product categories, extracts relevant product details, and saves them for further use.
Endpoint
GET /scrape

- **Description**: Triggers the scraping process for various product categories on Continente.pt.

- **Response**: Returns a success message once the scraping is completed.

- **Usage**:
    
      curl -X GET http://localhost:8080/scrape

    or simply access http://localhost:8080/scrape from a web browser.

## How It Works

The service navigates to multiple product category pages on **continente.pt**

It interacts with the website using Selenium WebDriver to load all products by clicking the "See More" button repeatedly.

Extracts product details using Jsoup, including:

- Product Name

- Price

- Product ID (PID)

- Quantity

Saves the extracted data into an Excel file named products-<category>.xlsx, using Apache POI.

## Technologies Used

- **Spring Boot**: For creating the REST API.

- **Selenium WebDriver**: For web automation and dynamic content loading.

- **Jsoup**: For parsing and extracting HTML data.

- **Apache POI**: For writing product details into Excel files.

## Configuration

Ensure Mozilla Firefox is installed.

Update the path to geckodriver.exe in ScrapingService.java:
System.setProperty("webdriver.gecko.driver", "D:\\scraper\\geckodriver.exe");

Modify the **EXCEL_FILE_PATH** if a different file location is needed.

## Notes

The scraping process may take several hours, depending on the number of products available in each category.

The service attempts to reject cookie pop-ups if they appear.

The extracted product details are stored in separate Excel files per category.

More categories can only be added programmatically.

## Troubleshooting

If scraping fails, check the geckodriver installation and paths.

Ensure the website structure has not changed; class names or selectors may need updating.

Verify that the server has permission to create and write Excel files.

## License

This project is intended for educational and research purposes only. Make sure to comply with the target website's terms of service before running the scraper (check **continente.pt/robots.txt**).


