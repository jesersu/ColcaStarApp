package com.colcastar.web;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import com.colcastar.web.Printer.MyPrinter;
import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.ImagePrintable;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.RawPrintable;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class JavaScriptInterface {
    private Context context;
    ActivityResultLauncher<Intent> someActivityResultLauncher;
    public JavaScriptInterface(Context context) {
        this.context = context;
        /*AppCompatActivity activity = (AppCompatActivity) this.context;
        activity.startActivityForResult();
        this.someActivityResultLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>(){
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            //doSomeOperations();
                        }
                    }
                }
        );*/
    }

    @JavascriptInterface
    public void saveDataTuBoletaPe(String deviceName, String numCopies, String automaticCut){
        Log.i("saveDataTuBoletaPe",deviceName);
        Log.i("saveDataTuBoletaPe",numCopies);
        Log.i("saveDataTuBoletaPe",automaticCut);
        //String printerAddress = "wew";
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        //Printooth.INSTANCE.setPrinter(deviceName, printerAddress);
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if(deviceName.equals(device.getName())){
                    //String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    //Printooth.INSTANCE.setPrinter(deviceName, deviceHardwareAddress);
                    Activity mActvity = (Activity) this.context;
                    SharedPreferences sharedPref = mActvity.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(context.getString(R.string.saved_device_address_imp), deviceHardwareAddress);
                    editor.putString(context.getString(R.string.saved_device_name_imp), device.getName());
                    editor.putString(context.getString(R.string.saved_num_copy_imp), numCopies);
                    editor.putString(context.getString(R.string.saved_automatic_cuts), automaticCut);
                    editor.apply();
                    Log.i("saveDataTuBoletaPe", "datos guardados");
                    break;
                }

            }
        }
        //Printooth.INSTANCE.getPairedPrinter().getName();
    }
    @JavascriptInterface
    public void getBase64FromBlobData(String base64Data) throws IOException {
        try {
            convertBase64StringToPdfAndStoreIt(base64Data);
        } catch (IOException e) {
            new Logger().log(context, e);
        }
    }

    @JavascriptInterface
    public void printMobileTuBoleta(String base64Data){
        Activity mActvity = (Activity) this.context;
        SharedPreferences sharedPref = mActvity.getPreferences(Context.MODE_PRIVATE);

        String printName = sharedPref.getString(context.getString(R.string.saved_device_name_imp), "");
        String printAddress = sharedPref.getString(context.getString(R.string.saved_device_address_imp), "");

        if(printName.isEmpty() || printAddress.isEmpty()){
            return;
        }
        Printooth printerInstance = Printooth.INSTANCE;
        printerInstance.removeCurrentPrinter();
        printerInstance.setPrinter(printName, printAddress);

        if(printerInstance.hasPairedPrinter()){
            Log.i("printMobileTuBoleta","impresora conectada");
            Log.i("printMobileTuBoleta",printerInstance.getPairedPrinter().getName());
            Log.i("printMobileTuBoleta",printerInstance.getPairedPrinter().getAddress());
            Log.i("printMobileTuBoleta",Printooth.INSTANCE.getPairedPrinter().getName());
            //Printooth.INSTANCE = printerInstance;
            String numCopiesStr = sharedPref.getString(context.getString(R.string.saved_num_copy_imp), "1");
            int numCopies = Integer.parseInt(numCopiesStr);
            this.printMobile(base64Data);


        }
        else {
            Log.i("printMobileTuBoleta","impresora no conectada");
        }

        //this.printMobile(base64Data);
    }

    @JavascriptInterface
    public void showDevices(){
        Log.i("mostrandoMensaje", "mensaje de pruebasss 2");
        AppCompatActivity activity = (AppCompatActivity) this.context;
        activity.startActivityForResult(new Intent(context, ScanningActivity.class),ScanningActivity.SCANNING_FOR_PRINTER);
    }

    @JavascriptInterface
    public void printMobile(String base64Data) {
        try {

            final File file = createFileFromBase64Pdf(base64Data);
            ArrayList<Printable> printables = new ArrayList<>();
            ArrayList<Bitmap> images = pdfToBitmap(file);

            for (Bitmap image : images) {
                printables.add(new ImagePrintable.Builder(image).build());
            }
            printables.add(new RawPrintable.Builder(new byte[]{0x1B, 0x40}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1B, 0x26}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1B, 0x40}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1B, 0x26}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1B, 0x40}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1B, 0x26}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());

            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());

            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());

            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());
            printables.add(new RawPrintable.Builder(new byte[]{0x1D, 0x2A}).build());


            MyPrinter myPrinter = new MyPrinter();
            Printing x = Printooth.INSTANCE.printer(myPrinter);
            x.setPrintingCallback(new PrintingCallback() {
                @Override
                public void disconnected() {
                    Log.i("connectingWithPrinter", "connectingWithPrinter: Desconectandos");
                }

                @Override
                public void connectingWithPrinter() {
                    Log.e("connectingWithPrinter", "connectingWithPrinter: Conectandose");
                }

                @Override
                public void printingOrderSentSuccessfully() {
                    Log.e("printingSuccessfully", "printingOrderSentSuccessfully: se termino de impirmir");
                }

                @Override
                public void connectionFailed(@NonNull String s) {
                    Log.e("connectionFailed", "connectionFailed: " + s);
                }

                @Override
                public void onError(@NonNull String s) {
                    Log.e("onError", "onError: " + s);
                }

                @Override
                public void onMessage(@NonNull String s) {
                    Log.e("onMessage", "onMessage:mensaje enviado " + s);
                }
            });
            x.print(printables);

        } catch (Exception e) {
            e.printStackTrace();
//            Log.e("printMobile", "getBase64StringFromBlobUrl: "+ blobUrl );
        }

    }

    @JavascriptInterface
    public void windowPrintMobile() {
        try {
            Log.e("printMobile", "windowPrintMobile: enra al metodo" + "");
            ;
        } catch (Exception e) {
            new Logger().log(context, e);
        }
    }

    public static String getBase64StringFromBlobUrl(String blobUrl) {
        Log.e("blobl ", "getBase64StringFromBlobUrl: " + blobUrl);
        if (blobUrl.startsWith("blob")) {
            return "javascript: var xhr = new XMLHttpRequest();" +
                    "xhr.open('GET', '" + blobUrl + "', true);" +
                    "xhr.setRequestHeader('Content-type','application/pdf');" +
                    "xhr.responseType = 'blob';" +
                    "xhr.onload = function(e) {" +
                    "    if (this.status == 200) {" +
                    "        var blobPdf = this.response;" +
                    "        var reader = new FileReader();" +
                    "        reader.readAsDataURL(blobPdf);" +
                    "        reader.onloadend = function() {" +
                    "            base64data = reader.result;" +
                    "            Android.getBase64FromBlobData(base64data);" +
                    "        }" +
                    "    }" +
                    "};" +
                    "xhr.send();" +
                    "window.print();";
        }
        return "javascript: console.log('It is not a Blob URL');";
    }

    private void convertBase64StringToPdfAndStoreIt(String base64PDf) throws IOException {
        final int notificationId = 1;
        final File file = createFileFromBase64Pdf(base64PDf);

        if (file.exists()) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            Uri apkURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(apkURI, MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf"));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            String CHANNEL_ID = "MYCHANNEL";
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "name", NotificationManager.IMPORTANCE_LOW);
                Notification notification = new Notification.Builder(context, CHANNEL_ID)
                        .setContentText("You have got something new!")
                        .setContentTitle("File downloaded")
                        .setContentIntent(pendingIntent)
                        .setChannelId(CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.sym_action_chat)
                        .build();
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                    notificationManager.notify(notificationId, notification);
                }

            } else {
                NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(android.R.drawable.sym_action_chat)
                        //.setContentIntent(pendingIntent)
                        .setContentTitle("MY TITLE")
                        .setContentText("MY TEXT CONTENT");

                if (notificationManager != null) {
                    notificationManager.notify(notificationId, b.build());
                    Handler h = new Handler();
                    long delayInMilliseconds = 1000;
                    h.postDelayed(new Runnable() {
                        public void run() {
                            notificationManager.cancel(notificationId);
                        }
                    }, delayInMilliseconds);
                }
            }
        }
        Toast.makeText(context, "PDF FILE DOWNLOADED!", Toast.LENGTH_SHORT).show();
    }

    private File createFileFromBase64Pdf(String base64PDf) throws IOException {

        final File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS) + "/YourFileName_" + "_.pdf");
        if (!file.exists()) {
            file.createNewFile();
        }
        byte[] pdfAsBytes = Base64.decode(base64PDf.replaceFirst("^data:application/pdf;base64,", ""), 0);
        FileOutputStream os;
        os = new FileOutputStream(file, false);
        os.write(pdfAsBytes);
        os.flush();
        return file;
    }

    private ArrayList<Bitmap> pdfToBitmap(File pdfFile) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));

            Bitmap bitmap;
            final int pageCount = renderer.getPageCount();

            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                int width = (int) ((337 * 1.24));
                int height = (int) (1267 * 1.24);

                //              bitmap = generateImageFromPdf("https://www3.gobiernodecanarias.org/medusa/ecoblog/rdiabeld/files/2012/10/mult.pdf", pageCount, width, height);
                bitmap = Bitmap.createBitmap((int) (width), (int) (height), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                bitmaps.add(bitmap);
//                // close the page
                page.close();
            }
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bitmaps;
    }
}