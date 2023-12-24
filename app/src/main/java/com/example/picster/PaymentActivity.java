package com.example.picster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.picster.model.CreditCard;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentActivity extends AppCompatActivity {
    Spinner bankSelector;
    String[] bankList = {"Select bank...", "ACB", "Agribank", "BIDV", "EXIM BANK", "HSBC", "IBK", "KBANK", "MB BANK", "OCB", "SCB", "SHINHAN BANK", "VIETCOM BANK", "WOORI BANK"};
    EditText cardNumber;
    Button paymentButton;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        bankSelector = findViewById(R.id.bankSelector);
        cardNumber = findViewById(R.id.cardNumber);
        paymentButton = findViewById(R.id.paymentButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bankSelector.setAdapter(adapter);
        bankSelector.setSelection(0, false);

        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedBank = bankSelector.getSelectedItem().toString();
                String enteredCardNumber = cardNumber.getText().toString().trim();

                if(selectedBank.equals("Select bank...")) {
                    Toast.makeText(PaymentActivity.this, "Please select a bank!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(enteredCardNumber.length() != 16) {
                    Toast.makeText(PaymentActivity.this, "Please enter valid credit card number!", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser user = auth.getCurrentUser();

                if(user != null) {
                    String email = user.getEmail();

                    CreditCard creditCard = new CreditCard(selectedBank, enteredCardNumber);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference userRef = db.collection("User").document(email);

                    userRef.update("creditCard", creditCard, "vip", true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(PaymentActivity.this, "Payment completed!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure during update
                                    Toast.makeText(PaymentActivity.this, "Failed to pay; " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(PaymentActivity.this, "User is not logged in!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}