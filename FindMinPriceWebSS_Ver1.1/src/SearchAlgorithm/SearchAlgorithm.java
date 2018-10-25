/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SearchAlgorithm;

import Common.XPathConstant;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author Minato
 */
public class SearchAlgorithm {

    // Xét giá nhỏ nhất để bỏ qua nó (chỉ lấy giá từ 500k trở lên)
    private static final int DEFAULT_PRICE_MIN = 500000;

    private static String listProductFail = "";
    private static StringBuilder textProductExport = new StringBuilder();

    public void searchProduct(String linkPathFile,
            String linkSaveFileProduct, int priceReduction) {

        long startTime = System.currentTimeMillis();

        FileInputStream fileInputStream;

        try {
            fileInputStream = new FileInputStream(linkPathFile);
            Reader reader = null;

            try {
                reader = new InputStreamReader(fileInputStream, "utf8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(SearchAlgorithm.class.getName()).log(
                        Level.SEVERE, null, ex);
            }

            BufferedReader inputText = new BufferedReader(reader);

            String tempProduct;
            int count = 1;

            try {
                while ((tempProduct = inputText.readLine()) != null) {

                    String nameProduct;

                    // remove 1234, bếp từ :v
                    if (tempProduct.contains(",")) {
                        nameProduct = tempProduct.substring(tempProduct.indexOf(
                                ",") + 1, tempProduct.length());
                    } else {
                        nameProduct = tempProduct;
                    }

                    // Nếu line = null => continue
                    if (nameProduct.length() == 0) {
                        continue;
                    }

                    System.out.println(count++ + ": " + nameProduct);

                    String product = nameProduct.replaceAll("[-–]", " ").
                            toLowerCase();
                    product = product.replaceAll("\\s\\s+", " ");

                    // Check tên sản phẩm chỉ với 2 mã code ở cuối
                    String codeOfProduct = "";

                    Pattern patternCode = Pattern.compile(
                            XPathConstant.REGEX.REGEX_GET_CODE_OF_PRODUCT);
                    Matcher matcherCode = patternCode.matcher(product);

                    while (matcherCode.find()) {
                        codeOfProduct = matcherCode.group(0);
                    }

                    // Code tìm sản phẩm trên website để sửa giá
                    String codeSearchProductInWeb = "";

                    Pattern patternCodeSearchProductInWeb = Pattern.compile(
                            XPathConstant.REGEX.REGEX_SEARCH_PRODUCT_IN_WEB);
                    Matcher matcherCodeSearchProductInWeb = patternCodeSearchProductInWeb.
                            matcher(nameProduct);

                    while (matcherCodeSearchProductInWeb.find()) {
                        codeSearchProductInWeb = matcherCodeSearchProductInWeb.
                                group(0);
                    }

                    if (codeSearchProductInWeb.equals("")) {
                        listProductFail += tempProduct + "\n";
                        System.out.println("Not found name product...");
                        continue;
                    }

                    long timeStart = System.currentTimeMillis();

                    // Get source code of websosanh
                    int defaultPrice = 100000000, price, sumPages = 1, numberPage = 1;

                    do {

                        String encodeSingleText = URLEncoder.encode(nameProduct.
                                trim(), "UTF-8");

                        encodeSingleText = "https://websosanh.vn/s/" + encodeSingleText;

                        String urlText = encodeSingleText + "?pi=" + (String.
                                valueOf(numberPage)) + ".htm";

                        URL url = new URL(urlText);
                        URLConnection connectURL = url.openConnection();
//                        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("", 3128));
//                        URLConnection connectURL = new URL(urlText).openConnection(proxy);

                        // Get inputStreamReader
                        try (BufferedReader inputURL = new BufferedReader(
                                new InputStreamReader(
                                        connectURL.getInputStream(),
                                        StandardCharsets.UTF_8))) {

                            String tempText;
                            String textLineStream = "";
                            String containsTextOfPage = "";

                            while ((tempText = inputURL.readLine()) != null) {
                                textLineStream += tempText;

                                // Get text chứa số trang của một sản phẩm
                                if (textLineStream.contains("data-page-index")) {
                                    containsTextOfPage += tempText;
                                }
                            }

                            // Get text chứa giá
                            Pattern pattern = Pattern.compile(
                                    XPathConstant.REGEX.REGEX_GET_BLOCK_CONTAINS_PRICE);
                            Matcher matcher = pattern.matcher(textLineStream.
                                    toLowerCase());

                            while (matcher.find()) {

                                String textContainsPrice = matcher.group(0);

                                // Hết Hàng
                                if (textContainsPrice.compareTo("out-of-stock") == 0) {
                                    continue;
                                }

                                // Get title product
                                Pattern pattern1 = Pattern.compile(
                                        XPathConstant.REGEX.REGEX_GET_TITLE_OF_PRODUCT);
                                Matcher matcher1 = pattern1.matcher(
                                        textContainsPrice);

                                // So sánh tên sản phẩm từ file với tên sản phẩm tên websosanh
                                String titleOfProduct = "";

                                while (matcher1.find()) {
                                    titleOfProduct = matcher1.group(2).
                                            replaceAll("[-–]", " ").
                                            toLowerCase();
                                    titleOfProduct = titleOfProduct.replaceAll(
                                            "\\s\\s+", " ");
                                }

                                // Nếu khác với tên sản phẩm trong file thì sẽ bỏ qua
                                if (!titleOfProduct.contains(codeOfProduct)) {
                                    continue;
                                }

                                textContainsPrice = textContainsPrice.
                                        replaceAll("giá từ", "");

                                // Tìm giá
                                Pattern pattern2 = Pattern.compile(
                                        XPathConstant.REGEX.REGEX_GET_PRICE_IN_BLOCK);
                                Matcher matcher2 = pattern2.matcher(
                                        textContainsPrice);

                                while (matcher2.find()) {
                                    // Continute if price set = 1
                                    if (matcher2.group(1).compareTo("1") == 0) {
                                        continue;
                                    }
                                    // Get price
                                    price = Integer.parseInt(matcher2.group(1).
                                            replace(".", ""));

                                    if (price <= DEFAULT_PRICE_MIN) {
                                        continue;
                                    }

                                    if (price < defaultPrice) {
                                        defaultPrice = price;
                                    }
                                }
                            }

                            // Page đầu tiên
                            if (sumPages == 1) {

                                // Chứa data-page-index => page > 1
                                if (!containsTextOfPage.equals("")) {

                                    // Get số trang của sản phẩm
                                    Pattern pattern3 = Pattern.compile(
                                            XPathConstant.REGEX.REGEX_GET_NUMBER_PAGES);
                                    Matcher matcher3 = pattern3.matcher(
                                            containsTextOfPage);

                                    while (matcher3.find()) {

                                        int tempNumberPages = Integer.parseInt(
                                                matcher3.group(2));

                                        if (sumPages < tempNumberPages) {
                                            sumPages = tempNumberPages;
                                        }
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            System.out.println("Error connect to server!");
                            listProductFail = listProductFail + tempProduct + "\n";
                            Logger.getLogger(SearchAlgorithm.class.getName()).
                                    log(Level.SEVERE, null, ex);
                        }

                        numberPage++;

                    } while (numberPage <= sumPages);

                    long timeEnd = System.currentTimeMillis();
                    System.out.println(
                            "Lay gia mat: " + (timeEnd - timeStart) + " milis!");

                    String priceMin = NumberFormat.getIntegerInstance(
                            Locale.GERMANY).format(defaultPrice) + " VND";

                    System.out.println(
                            "=========================================> Gia thap nhat la: " + priceMin);
                    String modifyPrice = NumberFormat.getIntegerInstance(
                            Locale.GERMANY).format(defaultPrice - priceReduction) + " đ";
                    textProductExport = textProductExport.append(tempProduct).append(",").append(
                            modifyPrice).append("\n");
                    System.out.println(tempProduct + "," + modifyPrice);
                }

            } catch (IOException ex) {
                Logger.getLogger(SearchAlgorithm.class.getName()).log(
                        Level.SEVERE, null, ex);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SearchAlgorithm.class.getName()).log(Level.SEVERE,
                    null, ex);
            JOptionPane.showMessageDialog(null,
                    "Lỗi đường dẫn khi chọn/lưu tệp. Vui lòng chọn lại!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!listProductFail.equals("")) {
            try {
                this.output(linkSaveFileProduct + "_Products_Error.txt",
                        listProductFail);
            } catch (IOException ex) {
                Logger.getLogger(SearchAlgorithm.class.getName()).log(
                        Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Lỗi ghi tệp!", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        long endTime = System.currentTimeMillis();
        long durationInMillis = endTime - startTime;

        long millis = durationInMillis % 1000;
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

        String timeDuration = String.format("%02d:%02d:%02d.%d", hour, minute,
                second, millis);

        JOptionPane.showMessageDialog(null,
                "Đã tìm kiếm xong!\nThời gian hoàn thành: " + timeDuration,
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);

        try {
            this.output(linkSaveFileProduct + ".txt", textProductExport.
                    toString());
        } catch (IOException ex) {
            Logger.getLogger(SearchAlgorithm.class.getName()).
                    log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null,
                    "Không thể lưu tệp",
                    "Lỗi xuất tệp!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void output(String linkFile, String contain) throws IOException {
        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(linkFile), "UTF-8"))) {
            out.write(contain);
        }
    }

}
