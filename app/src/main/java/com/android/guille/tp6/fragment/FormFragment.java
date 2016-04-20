package com.android.guille.tp6.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.guille.tp6.R;
import com.android.guille.tp6.activity.MainActivity;
import com.android.guille.tp6.entity.Persona;

public class FormFragment extends Fragment {

    private static View mRoot;
    private static Persona mUser = null;
    private static String mId;

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
                /*Persona user = new Persona(
                        nombre.getText().toString(),
                        apellido.getText().toString(),
                        mail.getText().toString());
                setUser(null);*/
                mUser.setApellido(apellido.getText().toString());
                mUser.setNombre(nombre.getText().toString());
                mUser.setEmail(mail.getText().toString());
                ((MainActivity) getActivity()).setUser(mUser, mUser.getId());
                //mPos = null;
                mUser = null;
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUser(null);
                //mPos = null;
                mUser = null;
                ((MainActivity) getActivity()).setUser(null, null);

            }
        });

        return mRoot;
    }

    private void setUser(Persona user) {
        if(mRoot != null) {
            EditText nombre = (EditText) mRoot.findViewById(R.id.editTxtNom);
            EditText apellido = (EditText) mRoot.findViewById(R.id.editTxtApe);
            EditText mail = (EditText) mRoot.findViewById(R.id.editTxtEmail);
            if (user == null) {
                nombre.setText("");
                apellido.setText("");
                mail.setText("");
            } else {
                nombre.setText(user.getNombre());
                apellido.setText(user.getApellido());
                mail.setText(user.getEmail());
            }
        }
    }

    public void selectUser(Persona user, Integer pos) {
        setUser(user);
        mUser = user;
        //mPos = pos;
    }
}
