# ScanQR
***一维码: EAN-8, EAN-13, UPC-A, UPC-E, Codabar, Code39, Code93, Code128, ISBN10, ISBN13, DataBar, DataBar Expanded, Interleaved 2 of 5***

***二维码: QR Code , PDF417***
***


**1. 在项目的libs目录中按以下层级添加libiconv.so，libscaninit.so，libsunmiscan.so和sunmiscan.jar四个库文件。**

**2. 在处理业务的代码中引入头文件和解码库，可以参照DEMO。**

    
     import com.sunmi.scan.Config;
     import com.sunmi.scan.Image;
     import com.sunmi.scan.ImageScanner;
     import com.sunmi.scan.Symbol;
     import com.sunmi.scan.SymbolSet; 
    
**3. 初始化和配置。**

    private ImageScanner scanner;//声明扫描器 
    
    scanner = new ImageScanner();//创建扫描器
    
    scanner.setConfig(0, Config.X_DENSITY, 2);//行扫描间隔
    
    scanner.setConfig(0, Config.Y_DENSITY, 2);//列扫描间隔
    
    scanner.setConfig(0, Config.ENABLE_MULTILESYMS, 0);
    
    //是否开启同一幅图一次解多个条码,0表示只解一个，1为多个
    
    scanner.setConfig(0, Config.ENABLE_INVERSE, 0);//是否解反色的条码
    
**4.传入图像数据和解码，以下的代码可以写在PreviewCallback.onPreviewFrame(byte[] data, Camera camera)方法中**

    /**
    *创建解码图像,width, height 分别为摄像头预览分辨率的宽度和高度,一般来说,分辨率越高图
    *像越清晰,但解码速度越慢。由于解码算法需要处理的是原始灰度数据,而预览图像的默认格式为 
    *YCbCr_420_SP,需要转换格式才能处理, 参数"Y800"表示待转换的图像格式。
    */
    Image source = new Image(width, height, "Y800");

    /**
    *设置扫描区域范围,为了较好的识读较长的一维码,扫描框的宽度不应过小,由于预览的图像为横屏, 
    *注意扫描区域需要转换为竖屏对应的位置 
    */
    Rect cropRect = finder_view.getScanImageRect(size.height, size.width);
    //finder_view为DEMO中自定义的扫码区域控件。
    source.setCrop(cropRect.top,cropRect.left,cropRect.height(),cropRect.width());

    /*填充图像数据,data 为摄像头原始数据*/
    source.setData(data); 

    /*解码,返回值为 0 代表失败,>0 表示成功*/
    int result = scanner.scanImage(source); 

**5.获取解码结果和条码类型**

      if (result != 0) {   
      SymbolSet syms = scanner.getResults();
          for (Symbol sym : syms) {   
            Log.i("sunmi", "码型:"+sym.getSymbolName());//条码类型,如“EAN-8”
            Log.i("sunmi","结果:"+sym.getResult())//获取解码结果字符串,这里就是要获取的结果
            
            }
    }       
