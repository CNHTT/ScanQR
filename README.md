# ScanQR
一维码: EAN-8, EAN-13, UPC-A, UPC-E, Codabar, Code39, Code93, Code128, ISBN10, ISBN13, DataBar, DataBar Expanded, Interleaved 2 of 5
二维码: QR Code , PDF417


1. 在项目的libs目录中按以下层级添加libiconv.so，libscaninit.so，libsunmiscan.so和sunmiscan.jar四个库文件。

2. 在处理业务的代码中引入头文件和解码库，可以参照DEMO。
    import com.sunmi.scan.Config;import com.sunmi.scan.Image;import com.sunmi.scan.ImageScanner; import com.sunmi.scan.Symbol;import com.sunmi.scan.SymbolSet; 
