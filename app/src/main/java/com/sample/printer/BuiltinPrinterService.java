package com.sample.printer;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import co.poynt.os.model.PrintedReceipt;
import co.poynt.os.model.PrintedReceiptV2;
import co.poynt.os.services.v1.IPoyntPrinterService;
import co.poynt.os.services.v1.IPoyntPrinterServiceListener;

public class BuiltinPrinterService extends Service {
    private static final String TAG = BuiltinPrinterService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    protected IPoyntPrinterService.Stub mBinder = new IPoyntPrinterService.Stub() {
        @Override
        public void printJob(String jobId, Bitmap bitmap, IPoyntPrinterServiceListener listener) throws RemoteException {
            Log.i(TAG, "printJob() - bitmap jobId=" + jobId);
            printImageBitmap(jobId, bitmap, listener);
        }

        @Override
        public void printReceiptJob(String jobId, PrintedReceipt printedReceipt, IPoyntPrinterServiceListener listener) throws RemoteException {
            Log.i(TAG, "printReceiptJob() - ReceiptV1 jobId=" + jobId);
            // Convert PrintedReceipt to bitmap using your own rendering utilities or OEM SDK if available
            // Example: Bitmap bitmap = YourRenderingUtil.createPrintableImage(printedReceipt);
            Bitmap bitmap = null;
            printImageBitmap(jobId, bitmap, listener);
        }

        @Override
        public void printJobByName(String provider, String jobId, Bitmap bitmap, IPoyntPrinterServiceListener listener) throws RemoteException {
            Log.i(TAG, "printJobByName() - provider=" + provider + ", jobId=" + jobId);
            printImageBitmap(jobId, bitmap, listener);
        }

        @Override
        public void printReceiptJobByName(String provider, String jobId, PrintedReceipt printedReceipt, IPoyntPrinterServiceListener listener) throws RemoteException {
            Log.i(TAG, "printReceiptJobByName() - provider=" + provider + ", jobId=" + jobId);
            // Convert PrintedReceipt to bitmap using your own rendering utilities or OEM SDK if available
            Bitmap bitmap = null;
            printImageBitmap(jobId, bitmap, listener);
        }

        @Override
        public void printReceipt(String jobId, PrintedReceiptV2 printedReceipt, IPoyntPrinterServiceListener listener, Bundle bundle) throws RemoteException {
            Log.i(TAG, "printReceipt() - ReceiptV2 jobId=" + jobId);
            // Convert PrintedReceiptV2 to bitmap using your own rendering utilities or OEM SDK if available
            Bitmap bitmap = null;
            printImageBitmap(jobId, bitmap, listener);
        }

        @Override
        public void printReceiptForPrinter(String provider, String jobId, PrintedReceiptV2 printedReceipt, IPoyntPrinterServiceListener listener, Bundle bundle) throws RemoteException {
            Log.i(TAG, "printReceiptForPrinter() - provider=" + provider + ", jobId=" + jobId);
            // Convert PrintedReceiptV2 to bitmap using your own rendering utilities or OEM SDK if available
            Bitmap bitmap = null;
            printImageBitmap(jobId, bitmap, listener);
        }
    };

    private void printImageBitmap(final String jobId, final Bitmap bitmap, final IPoyntPrinterServiceListener listener) {
        if (bitmap == null) {
            Log.w(TAG, "printImageBitmap() - bitmap is null");
            // Example: report an error to the listener if your implementation supports it
            // notifyFailure(listener, jobId, /*oemCode=*/-1, "Bitmap is null");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Integrators: Use your own in-built printer SDK and tools to integrate with your in-built printer here.
                    // Replace the pseudo-code below with your OEM printer calls.
                    // Example (pseudo-code):
                    // MyOemPrinter printer = MyOemPrinter.getInstance(getApplicationContext());
                    // printer.init();
                    // int resultCode = printer.printBitmap(bitmap, ALIGN_CENTER /* or desired alignment */);
                    // printer.feedPaper(20);
                    // printer.finish();
                    // String resultMessage = printer.getLastMessage();
                    // handlePrintResultWithListener(listener, jobId, resultCode, resultMessage);

                    Log.v(TAG, "printImageBitmap() - invoke OEM printer SDK here");
                } catch (Exception e) {
                    Log.e(TAG, "printImageBitmap() - error while printing", e);
                    // Map the thrown exception to an OEM/Generic error code and notify listener accordingly.
                    // handlePrintResultWithListener(listener, jobId, /*oemCode=*/-999, e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Listener handling breakdown for common printer statuses. Map your OEM SDK result codes here.
     * This is only guidance — replace the comments with actual listener method calls provided by Poynt.
     *
     * Expected mappings (examples):
     * - OUT_OF_PAPER:      Notify listener with an "out of paper" error/status
     * - COVER_OPEN:        Notify listener with a "cover open" error/status
     * - PAPER_JAM:         Notify listener with a "paper jam" error/status
     * - OVERHEAT:          Notify listener with an "overheated" warning/error
     * - LOW_BATTERY:       Notify listener with a "low battery" warning/error
     * - SUCCESS:           Notify listener that printing completed successfully
     * - UNKNOWN_ERROR:     Notify listener with a generic failure
     */
    private void handlePrintResultWithListener(IPoyntPrinterServiceListener listener,
                                               String jobId,
                                               int oemCode,
                                               String oemMessage) {
        // Define and map your OEM codes here (replace with your actual codes)
        // final int OEM_SUCCESS = 0;
        // final int OEM_OUT_OF_PAPER = 10;
        // final int OEM_COVER_OPEN = 11;  // aka empty/open cover
        // final int OEM_PAPER_JAM = 12;
        // final int OEM_OVERHEAT = 13;
        // final int OEM_LOW_BATTERY = 14;

        // switch (oemCode) {
        //     case OEM_SUCCESS:
        //         // Example: listener.onPrintJobCompleted(jobId);
        //         return;
        //     case OEM_OUT_OF_PAPER:
        //         // Example: listener.onPrinterStatusChanged(jobId, PrinterStatus.PRINTER_ERROR_OUT_OF_PAPER, oemMessage);
        //         return;
        //     case OEM_COVER_OPEN:
        //         // Example: listener.onPrinterStatusChanged(jobId, PrinterStatus.PRINTER_ERROR_COVER_OPEN, oemMessage);
        //         return;
        //     case OEM_PAPER_JAM:
        //         // Example: listener.onPrinterStatusChanged(jobId, PrinterStatus.PRINTER_ERROR_PAPER_JAM, oemMessage);
        //         return;
        //     case OEM_OVERHEAT:
        //         // Example: listener.onPrinterStatusChanged(jobId, PrinterStatus.PRINTER_WARNING_OVERHEAT, oemMessage);
        //         return;
        //     case OEM_LOW_BATTERY:
        //         // Example: listener.onPrinterStatusChanged(jobId, PrinterStatus.PRINTER_WARNING_LOW_BATTERY, oemMessage);
        //         return;
        //     default:
        //         // Example: listener.onPrintJobFailed(jobId, oemCode, (oemMessage != null ? oemMessage : "Unknown error"));
        // }
    }

    // Optional helpers to centralize success/failure notifications once you wire actual listener methods
    // private void notifySuccess(IPoyntPrinterServiceListener listener, String jobId) {
    //     // listener.onPrintJobCompleted(jobId);
    // }
    // private void notifyFailure(IPoyntPrinterServiceListener listener, String jobId, int code, String message) {
    //     // listener.onPrintJobFailed(jobId, code, message);
    // }
}
