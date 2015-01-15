package br.com.tairoroberto.testefacebooksdkandroidii;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;
import com.facebook.widget.WebDialog;

import java.util.Arrays;
import java.util.List;

// para gerar a hash no terminla linux usar o comando
// keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore | openssl sha1 -binary | openssl base64
// e gerar uma hash da palavra "android"

public class MainActivity extends ActionBarActivity {
    //obrigatorio o a verificação do ciclo de vida do login do facebook
    private UiLifecycleHelper uiHelper;

    private TextView txtNome,txtEmail,txtIdUsuario;
    private LoginButton btnLogin;
    private Button shareButton;
    ProfilePictureView pictureView;

    //Listener para verificar os eventos e mudanças no login do facebook
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState sessionState, Exception e) {
            onSessionStateChaged(session, sessionState, e);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializa o UiLifecycleHelper
        uiHelper = new UiLifecycleHelper(this,statusCallback);
        uiHelper.onCreate(savedInstanceState);

        txtNome = (TextView)findViewById(R.id.name);
        txtEmail = (TextView)findViewById(R.id.email);
        txtIdUsuario = (TextView)findViewById(R.id.id);
        pictureView = (ProfilePictureView)findViewById(R.id.profile_pic);
        btnLogin = (LoginButton)findViewById(R.id.btnLogin);
        shareButton = (Button)findViewById(R.id.btnCompartilhar);

        //Seta as permissões para o login
        btnLogin.setPublishPermissions(Arrays.asList("email","public_profile","user_friends"));
    }


    //Metodo para verificar o estado do login
    public void onSessionStateChaged(final Session session, SessionState sessionState, Exception e){
        if(session != null && session.isOpened()){
            Log.i("Script","Usuario Conectado...!");

            //Se usuario estiver conectado mostra o botão de compartilhar
            shareButton.setVisibility(View.VISIBLE);

                Request.newMeRequest(session,new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser graphUser, Response response) {
                    if (graphUser != null){
                        txtNome.setText(graphUser.getName());

                        //verifica se usuario tem email
                        if(graphUser.getProperty("email") != null){
                            txtEmail.setText(graphUser.getProperty("email").toString());
                        }else{
                            txtEmail.setText("Usuário não possui e-mail cadastrado no facebook");
                        }
                        txtIdUsuario.setText(graphUser.getId());

                        //imagem
                        pictureView.setProfileId(graphUser.getId());
                    }
                }
            }).executeAsync();

        }else{
            Log.i("Script","Usuario não Conectado...!");
            //Se usuario não estiver conectado esconder o botão de compartilhar
            shareButton.setVisibility(View.INVISIBLE);
        }

    }





    public void shareContent(View view){

        if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
            // Publish the post using the Share Dialog
            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                    .setLink("https://developers.facebook.com/android")
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());

        } else {
            // Fallback. For example, publish the post using the Feed Dialog
        }
    }



    //Ciclos de vida da activity e do UiLifecycleHelper

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Verificação se usuario está logado
        Session session = Session.getActiveSession();
        if(session != null && (session.isOpened() || session.isClosed())){
            onSessionStateChaged(session, session.getState(), null);
        }
        uiHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //uiHelper.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }
}
