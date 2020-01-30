package com.hypertrack.live.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hypertrack.live.HTMobileClient;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.utils.HTTextWatcher;

public class SignInFragment extends Fragment implements HTMobileClient.Callback {

    private static final String TAG = "SignInFragment";

    private EditText emailAddressEditText;
    private EditText passwordEditText;
    private View passwordClear;
    private View incorrect;

    private LoaderDecorator loader;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loader = new LoaderDecorator(getContext());

        emailAddressEditText = view.findViewById(R.id.email_address);
        passwordEditText = view.findViewById(R.id.password);
        passwordClear = view.findViewById(R.id.password_clear);
        incorrect = view.findViewById(R.id.incorrect);

        passwordEditText.addTextChangedListener(new HTTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                passwordClear.setVisibility(TextUtils.isEmpty(editable) ? View.INVISIBLE : View.VISIBLE);
            }
        });
        passwordClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordEditText.setText("");
            }
        });

        view.findViewById(R.id.sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incorrect.setVisibility(View.INVISIBLE);

                String email = emailAddressEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    loader.start();
                    HTMobileClient.getInstance(getContext()).signIn(email, password, SignInFragment.this);
                }
            }
        });

        view.findViewById(R.id.sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_frame, new SignUpFragment(), SignUpFragment.class.getSimpleName())
                            .addToBackStack(SignUpFragment.class.getSimpleName())
                            .commitAllowingStateLoss();
                }
            }
        });

    }

    @Override
    public void onSuccess(HTMobileClient mobileClient) {
        loader.stop();

        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onError(String message) {
        loader.stop();
        incorrect.setVisibility(View.VISIBLE);
    }
}
