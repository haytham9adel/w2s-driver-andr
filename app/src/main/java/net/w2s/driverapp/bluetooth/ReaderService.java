package net.w2s.driverapp.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.Acr3901us1Reader;
import com.acs.bluetooth.BluetoothReader;
import com.acs.bluetooth.BluetoothReaderGattCallback;
import com.acs.bluetooth.BluetoothReaderManager;

import net.w2s.driverapp.R;

import java.io.UnsupportedEncodingException;


public class ReaderService extends Service {


    public final String TAG = "ReaderService" ;
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    /* Default master key. */
    private static final String DEFAULT_1255_MASTER_KEY = "ACR1255U-J1 Auth";

    /* Reader to be connected. */
    private String mDeviceAddress;
    private int mConnectState = BluetoothReader.STATE_DISCONNECTED;
    /* Detected reader. */
    private BluetoothReader mBluetoothReader;
    /* ACS Bluetooth reader library. */
    private BluetoothReaderManager mBluetoothReaderManager;
    private BluetoothReaderGattCallback mGattCallback;
    /* Bluetooth GATT client. */
    private BluetoothGatt mBluetoothGatt;

    private static final byte[] AUTO_POLLING_START = {(byte) 0xE0, 0x00, 0x00, 0x40, 0x01};

    private boolean isReadyToRead = false;

    Intent progressIntent; // Implemented in DeviceScanActivity

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {

        progressIntent = new Intent("progress");
        progressIntent.setAction("progress");

        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

          /* Initialize BluetoothReaderGattCallback. */
        mGattCallback = new BluetoothReaderGattCallback();

        /* Register BluetoothReaderGattCallback's listeners */
        mGattCallback.setOnConnectionStateChangeListener(new BluetoothReaderGattCallback.OnConnectionStateChangeListener() {

            @Override
            public void onConnectionStateChange(
                    final BluetoothGatt gatt, final int state,
                    final int newState) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (newState == BluetoothReader.STATE_CONNECTED) {
                                /* Detect the connected reader. */
                            if (mBluetoothReaderManager != null) {
                                mBluetoothReaderManager.detectReader(gatt, mGattCallback);
                                sendBroadcast(progressIntent.putExtra("msg", "detecting reader..."));
                            }
                        } else if (newState == BluetoothReader.STATE_DISCONNECTED) {
                            isReadyToRead = false;
                            mBluetoothReader = null;
                            /** Release resources occupied by Bluetooth GATT client. */
                            if (mBluetoothGatt != null) {
                                mBluetoothGatt.close();
                                mBluetoothGatt = null;
                            }
                        }
                    }
                });

            }
        });

        /* Initialize mBluetoothReaderManager. */
        mBluetoothReaderManager = new BluetoothReaderManager();

        /* Register BluetoothReaderManager's listeners */
        mBluetoothReaderManager.setOnReaderDetectionListener(new BluetoothReaderManager.OnReaderDetectionListener() {

            @Override
            public void onReaderDetection(BluetoothReader reader) {
                mBluetoothReader = reader;
                setListener(reader);
                activateReader(reader);
            }
        });

        /* Connect the reader. */
        connectReader();
        return START_NOT_STICKY;
    }

    private void authenticate() {
        if (mBluetoothReader == null) {
            return;
        }
               /* Retrieve master key from edit box. */
        try {
            byte masterKey[] = Utils.getEditTextinHexBytes(Utils
                    .toHexString(DEFAULT_1255_MASTER_KEY
                            .getBytes("UTF-8")));
            if (masterKey != null && masterKey.length > 0) {
                mBluetoothReader.authenticate(masterKey);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*
        * Create a GATT connection with the reader. And detect the connected reader
        * once service list is available.
        */
    private boolean connectReader() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
              Log.w(TAG, "Unable to initialize BluetoothManager.");
            return false;
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
               Log.w(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        /*
         * Connect Device.
         */
        /* Clear old GATT connection. */
        if (mBluetoothGatt != null) {
              Log.i(TAG, "Clear old GATT connection");
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        /* Create a new connection. */
        final BluetoothDevice device = bluetoothAdapter
                .getRemoteDevice(mDeviceAddress);

        if (device == null) {
             Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }

        /* Connect to GATT server. */
        //   updateConnectionState(BluetoothReader.STATE_CONNECTING);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        return true;
    }

    /* Start the process to enable the reader's notifications. */
    private void activateReader(BluetoothReader reader) {
        if (reader == null) {
            return;
        }

        if (reader instanceof Acr3901us1Reader) {
            /* Start pairing to the reader. */
            ((Acr3901us1Reader) mBluetoothReader).startBonding();
        } else if (mBluetoothReader instanceof Acr1255uj1Reader) {
            /* Enable notification. */
            mBluetoothReader.enableNotification(true);
        }
    }

    private void transmitEscapeCommand() {
         /* Check for detected reader. */
        if (mBluetoothReader == null) {
            return;
        }
                /* Retrieve escape command from edit box. */
        byte escapeCommand[] = Utils.getEditTextinHexBytes("E0 00 00 48 04");

                    /* Transmit escape command. */
        if (!mBluetoothReader.transmitEscapeCommand(escapeCommand)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ReaderService.this, getString(R.string.card_reader_not_ready), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /*
    * Update listener
    */
    private void setListener(BluetoothReader reader) {
        /* Update status change listener */

        ((Acr1255uj1Reader) mBluetoothReader)
                .setOnBatteryLevelChangeListener(new Acr1255uj1Reader.OnBatteryLevelChangeListener() {

                    @Override
                    public void onBatteryLevelChange(
                            BluetoothReader bluetoothReader,
                            final int batteryLevel) {
                    }
                });

        mBluetoothReader.setOnCardStatusChangeListener(new BluetoothReader.OnCardStatusChangeListener() {

            @Override
            public void onCardStatusChange(
                    BluetoothReader bluetoothReader, final int sta) {
 /* Check for detected reader. */
                if (mBluetoothReader == null) {
                    return;
                }
                final String s = getCardStatusString(sta);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //    Toast.makeText(ReaderService.this, s, Toast.LENGTH_SHORT).show();
                        if (isReadyToRead) {
                            if (sta == BluetoothReader.CARD_STATUS_PRESENT) {
                /* Retrieve APDU command from edit box. */
                                byte apduCommand[] = Utils.getEditTextinHexBytes("FF B0 00 04 10");

                                if (apduCommand != null && apduCommand.length > 0) {
                    /* Clear response field for result of APDU. */
                    /* Transmit APDU command. */
                                    mBluetoothReader.transmitApdu(apduCommand);
                                }
                            }
                        }
                    }
                });
            }
        });

        /* Wait for authentication completed. */
        mBluetoothReader
                .setOnAuthenticationCompleteListener(new BluetoothReader.OnAuthenticationCompleteListener() {

                    @Override
                    public void onAuthenticationComplete(
                            BluetoothReader bluetoothReader, final int errorCode) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (errorCode == BluetoothReader.ERROR_SUCCESS) {
                                    if (mBluetoothReader == null) {
                                        return;
                                    }
                                    Toast.makeText(ReaderService.this, "authentication done", Toast.LENGTH_SHORT).show();
                                    sendBroadcast(progressIntent.putExtra("msg", "start pulling...."));
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mBluetoothReader.transmitEscapeCommand(AUTO_POLLING_START);

                                            transmitEscapeCommand();
                                        }
                                    }, 1000);
                                } else {
                                    Toast.makeText(ReaderService.this, "authentication faild.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                });

        /* Wait for receiving ATR string. */
        mBluetoothReader.setOnAtrAvailableListener(new BluetoothReader.OnAtrAvailableListener() {

            @Override
            public void onAtrAvailable(BluetoothReader bluetoothReader,
                                       final byte[] atr, final int errorCode) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        isReadyToRead = true;
                        sendBroadcast(progressIntent.putExtra("msg", "dismiss"));
                        Toast.makeText(ReaderService.this, "Device is ready to user", Toast.LENGTH_SHORT).show();
                        if (atr == null) {
                              Toast.makeText(ReaderService.this, "atr null", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                String str = new String(atr, "UTF-8");
                                Log.e("data", str);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

        /* Wait for power off response. */
        mBluetoothReader.setOnCardPowerOffCompleteListener(new BluetoothReader.OnCardPowerOffCompleteListener() {

            @Override
            public void onCardPowerOffComplete(
                    BluetoothReader bluetoothReader, final int result) {

            }
        });

        /* Wait for response APDU. */
        mBluetoothReader
                .setOnResponseApduAvailableListener(new BluetoothReader.OnResponseApduAvailableListener() {

                    @Override
                    public void onResponseApduAvailable(
                            BluetoothReader bluetoothReader, final byte[] apdu,
                            final int errorCode) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                String data = getResponseString(apdu, errorCode);
                                Intent intent = new Intent("com.card_data_receiver"); // Implemented in StudentsReportFragment
                                intent.setAction("com.card_data_receiver");
                                intent.putExtra("data", data);
                                sendBroadcast(intent);
                            }
                        });
                    }

                });

        /* Wait for escape command response. */
        mBluetoothReader
                .setOnEscapeResponseAvailableListener(new BluetoothReader.OnEscapeResponseAvailableListener() {

                    @Override
                    public void onEscapeResponseAvailable(
                            BluetoothReader bluetoothReader,
                            final byte[] response, final int errorCode) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (mBluetoothReader == null) {
                                    return;
                                }
                                sendBroadcast(progressIntent.putExtra("msg", "power on card...."));
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mBluetoothReader.powerOnCard();
                                    }
                                }, 1000);
                            }
                        });
                    }
                });

        /* Wait for device info available. */
        mBluetoothReader
                .setOnDeviceInfoAvailableListener(new BluetoothReader.OnDeviceInfoAvailableListener() {

                    @Override
                    public void onDeviceInfoAvailable(
                            BluetoothReader bluetoothReader, final int infoId,
                            final Object o, final int status) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (status != BluetoothGatt.GATT_SUCCESS) {
                                    //Toast.makeText(ReaderActivity.this,
                                    //         "Failed to read device info!",
                                    //         Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                switch (infoId) {
                                    case BluetoothReader.DEVICE_INFO_SYSTEM_ID: {
                                        //      mTxtSystemId.setText(Utils
                                        //              .toHexString((byte[]) o));
                                    }
                                    break;
                                    case BluetoothReader.DEVICE_INFO_MODEL_NUMBER_STRING:
                                        //       mTxtModelNo.setText((String) o);
                                        break;
                                    case BluetoothReader.DEVICE_INFO_SERIAL_NUMBER_STRING:
                                        //       mTxtSerialNo.setText((String) o);
                                        break;
                                    case BluetoothReader.DEVICE_INFO_FIRMWARE_REVISION_STRING:
                                        //       mTxtFirmwareRev.setText((String) o);
                                        break;
                                    case BluetoothReader.DEVICE_INFO_HARDWARE_REVISION_STRING:
                                        //       mTxtHardwareRev.setText((String) o);
                                        break;
                                    case BluetoothReader.DEVICE_INFO_MANUFACTURER_NAME_STRING:
                                        //     mTxtManufacturerName.setText((String) o);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                    }

                });

        /* Wait for battery level available. */
        if (mBluetoothReader instanceof Acr1255uj1Reader) {
            ((Acr1255uj1Reader) mBluetoothReader)
                    .setOnBatteryLevelAvailableListener(new Acr1255uj1Reader.OnBatteryLevelAvailableListener() {

                        @Override
                        public void onBatteryLevelAvailable(
                                BluetoothReader bluetoothReader,
                                final int batteryLevel, int status) {
                            //      Log.i(TAG, "mBatteryLevelListener data: "
                            //              + batteryLevel);



                        }

                    });
        }

        /* Handle on battery status available. */
        if (mBluetoothReader instanceof Acr3901us1Reader) {
            ((Acr3901us1Reader) mBluetoothReader)
                    .setOnBatteryStatusAvailableListener(new Acr3901us1Reader.OnBatteryStatusAvailableListener() {

                        @Override
                        public void onBatteryStatusAvailable(
                                BluetoothReader bluetoothReader,
                                final int batteryStatus, int status) {

                        }

                    });
        }

        /* Handle on slot status available. */
        mBluetoothReader
                .setOnCardStatusAvailableListener(new BluetoothReader.OnCardStatusAvailableListener() {

                    @Override
                    public void onCardStatusAvailable(
                            BluetoothReader bluetoothReader,
                            final int cardStatus, final int errorCode) {

                    }

                });

        mBluetoothReader
                .setOnEnableNotificationCompleteListener(new BluetoothReader.OnEnableNotificationCompleteListener() {

                    @Override
                    public void onEnableNotificationComplete(
                            BluetoothReader bluetoothReader, final int result) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (result != BluetoothGatt.GATT_SUCCESS) {
                                    /* Fail */
                                    //   Toast.makeText(
                                    //            ReaderActivity.this,
                                    //           "The device is unable to set notification!",
                                    //           Toast.LENGTH_SHORT).show();
                                } else {
                                    /*Toast.makeText(ReaderService.this,
                                            "The device is ready to use!",
                                            Toast.LENGTH_SHORT).show();*/
                                    sendBroadcast(progressIntent.putExtra("msg", "authenticating..."));
                                    authenticate();
                                }
                            }
                        });
                    }

                });
    }


    /* Get the Response string. */
    private String getResponseString(byte[] response, int errorCode) {
        String output = "";
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            if (response != null && response.length > 0) {
                try {
                    String s = Utils.toHexString(response);
                    s = s.replaceAll(" ", "");
                    s = s.replaceAll("F", "");
                    s = s.substring(0, s.length() - 4);
                    StringBuilder s1 = new StringBuilder();
                    int a = 0;
                    for (int i = 0; i < s.length(); i++) {
                        if (s.substring(i, i + 1).equals("0") || s.substring(i, i + 1).equals("1")) {
                            a = a + 8;
                            String content = s.substring(i, a);
                            if (content.matches("^[01]+$")) {
                                // accept this input
                                s1.append(content);
                                s1.append(" ");
                                i = a;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }

                    String input = s1.toString().trim(); // Binary input as String
                    output = int2str(input);
                   /* StringBuilder sb = new StringBuilder(); // Some place to store the chars
                    sb.append((char) Integer.parseInt(input, 2));

                    final String output = sb.toString(); // Output text (t)
*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //return Utils.toHexString(response);
                return output;
            }
            return "";
        }
        return getErrorString(errorCode);
    }

    /* Get the Error string. */
    private String getErrorString(int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            return "";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_CHECKSUM) {
            return "The checksum is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA_LENGTH) {
            return "The data length is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_COMMAND) {
            return "The command is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_UNKNOWN_COMMAND_ID) {
            return "The command ID is unknown.";
        } else if (errorCode == BluetoothReader.ERROR_CARD_OPERATION) {
            return "The card operation failed.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_REQUIRED) {
            return "Authentication is required.";
        } else if (errorCode == BluetoothReader.ERROR_LOW_BATTERY) {
            return "The battery is low.";
        } else if (errorCode == BluetoothReader.ERROR_CHARACTERISTIC_NOT_FOUND) {
            return "Error characteristic is not found.";
        } else if (errorCode == BluetoothReader.ERROR_WRITE_DATA) {
            return "Write command to reader is failed.";
        } else if (errorCode == BluetoothReader.ERROR_TIMEOUT) {
            return "Timeout.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_FAILED) {
            return "Authentication is failed.";
        } else if (errorCode == BluetoothReader.ERROR_UNDEFINED) {
            return "Undefined error.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA) {
            return "Received data error.";
        }
        return "Unknown error.";
    }

    public static String int2str(String s) {
        String[] ss = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ss.length; i++) {
            sb.append((char) Integer.parseInt(ss[i], 2));
        }
        return sb.toString();
    }

    /* Get the Card status string. */
    private String getCardStatusString(int cardStatus) {
        if (cardStatus == BluetoothReader.CARD_STATUS_ABSENT) {
            return "Absent.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_PRESENT) {
            return "Present.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWERED) {
            return "Powered.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWER_SAVING_MODE) {
            return "Power saving mode.";
        }
        return "The card status is unknown.";
    }
}
