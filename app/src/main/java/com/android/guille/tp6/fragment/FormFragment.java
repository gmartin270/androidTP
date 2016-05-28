package com.android.guille.tp6.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.guille.tp6.R;
import com.android.guille.tp6.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class FormFragment extends Fragment {

    private static View mRoot;
    private static JSONObject mUser = null;
    private static String mId;
    private static String mMongoId;

    public FormFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mUser != null) {
            setUser(mUser);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRoot == null) {
            mRoot = inflater.inflate(R.layout.fragment_form, container, false);
        }

        Button aceptar = (Button) mRoot.findViewById(R.id.btnAceptar);
        Button cancelar = (Button) mRoot.findViewById(R.id.btnCancelar);

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nombre = (EditText) mRoot.findViewById(R.id.editTxtNom);
                EditText apellido = (EditText) mRoot.findViewById(R.id.editTxtApe);
                EditText mail = (EditText) mRoot.findViewById(R.id.editTxtEmail);

                try {
                    mUser = new JSONObject();
                    mUser.put("apellido", apellido.getText().toString());
                    mUser.put("nombre", nombre.getText().toString());
                    mUser.put("mail", mail.getText().toString());
                    mUser.put("_id", mId);
                    mUser.put("mongo_id", mMongoId);
                    ((MainActivity) getActivity()).setUser(mUser, mId);
                    mId = null;
                    mMongoId = null;

                }catch (JSONException e){
                    Log.e("Form Fragment - onClick", e.getMessage());
                }

                mUser = null;
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUser(null);
                mId = null;
                mMongoId = null;
                mUser = null;
                ((MainActivity) getActivity()).setUser(null, null);

            }
        });

        return mRoot;
    }

    private void setUser(JSONObject user) {
        if(mRoot != null) {
            EditText nombre = (EditText) mRoot.findViewById(R.id.editTxtNom);
            EditText apellido = (EditText) mRoot.findViewById(R.id.editTxtApe);
            EditText mail = (EditText) mRoot.findViewById(R.id.editTxtEmail);
            if (user == null) {
                nombre.setText("");
                apellido.setText("");
                mail.setText("");
            } else {
                try {
                    nombre.setText(user.getString("nombre"));
                    apellido.setText(user.getString("apellido"));
                    mail.setText(user.getString("mail"));
                    mId = user.getString("_id");
                    mMongoId = user.optString("mongo_id");
                }catch (JSONException e){
                    Log.e("Form Fragment - setUser", e.getMessage());
                }
            }
        }
    }

    public void selectUser(JSONObject user) {
        setUser(user);
        mUser = user;
    }
}
