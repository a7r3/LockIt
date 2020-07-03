package nooblife.lockit.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.github.druk.rx2dnssd.BonjourService;
import com.github.druk.rx2dnssd.Rx2Dnssd;
import com.github.druk.rx2dnssd.Rx2DnssdBindable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.Executors;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LockItServer {

    public final String TAG = getClass().getSimpleName();
    ServerSocket serverSocket;
    Disposable registerDisposable;
    private int serverPort = -1;
    private boolean isLocked;
    private ServerEventListener listener;
    private Context context;
    private boolean isRunning;
    private boolean isInPairingMode;

    private static LockItServer instance;

    private OnPairEventListener onPairEventListener;
    private OnLockEventListener onLockEventListener;
    private OnUnlockEventListener onUnlockEventListener;

    @Deprecated
    private LockItServer(Context context, ServerEventListener serverEventListener) {
        this.context = context;
        this.listener = serverEventListener;
        this.isLocked = false;
        this.isRunning = false;
        this.isInPairingMode = false;
    }

    private LockItServer(Context context) {
        this.context = context;
        this.isLocked = false;
        this.isRunning = false;
        this.isInPairingMode = false;
    }

    @Deprecated
    public static LockItServer initialize(Context context, @NonNull ServerEventListener serverEventListener) {
        if (instance == null)
            instance = new LockItServer(context, serverEventListener);
        return instance;
    }

    public static LockItServer initialize(Context context) {
        if (instance == null)
            instance = new LockItServer(context);
        return instance;
    }

    public LockItServer onPair(OnPairEventListener listener) {
        setInPairingMode();
        this.onPairEventListener = listener;
        return this;
    }

    public LockItServer onLock(OnLockEventListener listener) {
        this.onLockEventListener = listener;
        return this;
    }

    public LockItServer onUnlock(OnUnlockEventListener listener) {
        this.onUnlockEventListener = listener;
        return this;
    }

    public void start() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                isLocked = true;
                serverSocket = new ServerSocket(0, 1, InetAddress.getByName("0.0.0.0"));
                Log.i(TAG, "Server: Started @ " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
                serverPort = serverSocket.getLocalPort();
                startDNSSDService();

                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());

                    Log.i(TAG, "Server: Client Connected: " + socket.getInetAddress().getHostAddress());

                    String action = reader.readLine();
                    Log.i(TAG, "Server: Client says: " + action);
                    if (action.equals("lock") && !isLocked) {
                        isLocked = true;
                        if (onLockEventListener != null) onLockEventListener.onLock();
                    } else if (action.equals("unlock") && isLocked) {
                        isLocked = false;
                        if (onUnlockEventListener != null) onUnlockEventListener.onUnlock();
                    } else if (action.equals("pair")) {
                        String dedicatedServiceId = UUID.randomUUID().toString();
                        writer.println(dedicatedServiceId);
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .edit().putString(Constants.PREF_LOCKIT_RC_SERVICE_ID, dedicatedServiceId).apply();
                        if (onPairEventListener != null) onPairEventListener.onPair();
                    } else if (action.equals("connect")) {
                        writer.println(isLocked);
                    } else {
                        writer.println("failed");
                    }

                    writer.flush();
                    reader.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void startDNSSDService() {
        Rx2Dnssd rx2Dnssd = new Rx2DnssdBindable(context);
        BonjourService bs = new BonjourService.Builder(
                0,
                0,
                Build.MODEL,
                getServiceId(),
                null
        ).port(serverPort).build();

        registerDisposable = rx2Dnssd.register(bs)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(bonjourService -> {
                    Log.i(TAG, "DNSSD Service: Registered @ " + bonjourService.getRegType());
                    isRunning = true;
                }, Throwable::printStackTrace);
    }

    public void stop() {
        if (!isRunning) return;

        try {
            registerDisposable.dispose();
            serverSocket.close();
            Log.i(TAG, "Server: is stopped");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRunning = false;
        }
    }

    private String getServiceId() {
        return String.format(Constants.LOCKIT_SERVICE_TEMPLATE,
                Constants.LOCKIT_DEFAULT_SERVICE_ID);
    }

    private void setInPairingMode() {
        isInPairingMode = true;
    }

    @FunctionalInterface
    public interface OnLockEventListener {
        void onLock();
    }

    @FunctionalInterface
    public interface OnUnlockEventListener {
        void onUnlock();
    }

    @FunctionalInterface
    public interface OnPairEventListener {
        void onPair();
    }

    @Deprecated
    public interface ServerEventListener {
        void onLock();

        void onUnlock();

        void onPair();
    }

}
