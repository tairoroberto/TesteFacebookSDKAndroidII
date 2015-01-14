package br.com.tairoroberto.testefacebooksdkandroidii;


import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;

import java.util.Arrays;
import java.util.List;

// para gerar a hash no terminla linux usar o comando
// keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore | openssl sha1 -binary | openssl base64
// e gerar uma hash da palavra "android"

public class MainActivity extends ActionBarActivity {
    //obrigatorio o a verificação do ciclo de vida do login do facebook
    private UiLifecycleHelper  lifecycleHelper;

    //Listener para verificar os eventos e mudanças no login do facebook
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState sessionState, Exception e) {
            onSessionStateChaged(session, sessionState, e);
        }
    };

    private TextView txtNome,txtEmail,txtIdUsuario;
    private LoginButton btnLogin;
    ProfilePictureView pictureView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializa o UiLifecycleHelper
        lifecycleHelper = new UiLifecycleHelper(this,statusCallback);
        lifecycleHelper.onCreate(savedInstanceState);

        txtNome = (TextView)findViewById(R.id.name);
        txtEmail = (TextView)findViewById(R.id.email);
        txtIdUsuario = (TextView)findViewById(R.id.id);
        pictureView = (ProfilePictureView)findViewById(R.id.profile_pic);
        btnLogin = (LoginButton)findViewById(R.id.btnLogin);

        //Seta as permissões para o login
        btnLogin.setPublishPermissions(Arrays.asList("email","public_profile","user_friends"));

    }

    //Metodo para verificar o estado do login
    public void onSessionStateChaged(final Session session, SessionState sessionState, Exception e){
        if(session != null && session.isOpened()){
            Log.i("Script","Usuario Conectado...!");

            Request.newMeRequest(session,new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser graphUser, Response response) {
                    if (graphUser != null){
                        txtNome.setText(graphUser.getName());

                        //verifica se usuario tem email
                        if(graphUser.getProperty("email") != null){

                        }else{
                            txtEmail.setText("Usuário não possui e-mail cadastrado no facebook");
                        }
                        txtIdUsuario.setText(graphUser.getId());

                        //imagem
                        pictureView.setProfileId(graphUser.getId());

                        //mostra os amigos
                        getFriends(session);
                    }
                }
            }).executeAsync();

        }else{
            Log.i("Script","Usuario não Conectado...!");
        }

    }

    //metodo para pegar a lista de usuarios
    public void getFriends(Session session){
        Request.newMyFriendsRequest(session, new Request.GraphUserListCallback() {
            @Override
            public void onCompleted(List<GraphUser> graphUsers, Response response) {
                if(graphUsers != null){
                    Log.i("Script","Friends: "+ graphUsers.size());
                }
                Log.i("Script","Response: "+ response);
            }
        }).executeAsync();
    }

    //Ciclos de vida da activity e do UiLifecycleHelper

    @Override
    protected void onResume() {
        super.onResume();
        lifecycleHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Verificação se usuario está logado
        Session session = Session.getActiveSession();
        if(session != null && (session.isOpened() || session.isClosed())){
            onSessionStateChaged(session, session.getState(), null);
        }
        lifecycleHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifecycleHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        lifecycleHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        lifecycleHelper.onActivityResult(requestCode,resultCode,data);
    }
}
