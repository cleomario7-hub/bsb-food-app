package br.com.bsbfoods.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;

/**
 * BSB Food - MainActivity (Capacitor).
 *
 * 1) Cria o canal de notificação "corridas" (importância alta, som e vibração) assim que o app abre.
 *    É o mesmo ID usado pelo plugin BSB Notificações no servidor (android.notification.channel_id).
 * 2) Pede a permissão POST_NOTIFICATIONS (Android 13+) ao abrir o app.
 * 3) Expõe window.BSBPrint.print(html, titulo) para o site imprimir comprovantes/etiquetas
 *    pelo serviço de impressão do Android (o WebView não imprime sozinho).
 */
public class MainActivity extends BridgeActivity {

    private static final String CHANNEL_ID = "corridas";
    private static final int REQ_NOTIF = 7301;

    // Mantém a referência do WebView de impressão até terminar (evita ser coletado antes).
    private WebView printWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        criarCanalCorridas();
        pedirPermissaoNotificacao();
        try {
            getBridge().getWebView().addJavascriptInterface(new PrintBridge(), "BSBPrint");
        } catch (Throwable ignored) {
            // Sem a ponte, o site só mostra o comprovante em tela cheia (sem botão Imprimir).
        }
    }

    private void criarCanalCorridas() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null || nm.getNotificationChannel(CHANNEL_ID) != null) return;

        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Corridas e pedidos", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Avisos de novas corridas e pedidos");
        ch.enableVibration(true);
        ch.enableLights(true);
        ch.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
        Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ch.setSound(som, new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        nm.createNotificationChannel(ch);
    }

    private void pedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT < 33) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) return;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
    }

    /** Ponte JavaScript -> impressão do Android. */
    private class PrintBridge {
        @JavascriptInterface
        public void print(final String html, final String titulo) {
            runOnUiThread(() -> {
                final String nome = (titulo == null || titulo.isEmpty()) ? "Comprovante BSB Food" : titulo;
                printWebView = new WebView(MainActivity.this);
                printWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                        if (pm == null) return;
                        PrintDocumentAdapter adapter = view.createPrintDocumentAdapter(nome);
                        pm.print(nome, adapter, new PrintAttributes.Builder().build());
                    }
                });
                printWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            });
        }
    }
}
