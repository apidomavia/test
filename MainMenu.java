package com.ioocare.app_cust;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;


public class MainMenu extends AppCompatActivity {

    private KonfettiView konfettiView;
    private Shape.DrawableShape drawableShape = null;
    private View[] cards = new View[13];
    APIRequest reqGetAbsenceInfo,reqGetLatestEndDate,reqGetEndDate,reqGetUserNotes;
    private CardView planning,boxabsence,boxFinPlanIntervention;
    private TextView HiUser,Notation,textDateReprise,textcircleConv,nbPropositionsTextView,nameNextIntervenante,dateNextIntervenante,NmbreDaysLeft,dateEndIntervention,textWallet,choiceQuest;
    private ImageView notification, cricleredConv,icon_wallet;
    private FrameLayout imagehorlogeContainer;
    private Button buttonAbsence, buttonAides, buttonFinPlanIntervention;
    private int[] cardsIds;
    private boolean ischeckInterventionChoice;
    private static final String CHANNEL_ID = "default_channel_id";
    private static final int NOTIFICATION_ID = 1;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int REQUEST_CODE_SCHEDULE_EXACT_ALARM = 2;
    APIRequest reqCheckActivePlanning,reqGetNextOccurrence,reqGetProposals,reqNumberNewConv;
    private boolean isPlanningActive;
    private LayoutInflater inflater;
    private String idOccurrence;
    private APIRequest reqHasPlanning;
    // InApp notifications
    private View notification_inapp_container, notification_inapp_counter_container;
    private TextView notification_inapp_counter_text, text_aides;
    private APIRequest reqGetNotificationsInapp;
    private Integer planningStatus = null;
    private Integer noplanStatus = null;

    private Runnable[] cardsFunctions = new Runnable[]{
            () -> {
                if (idOccurrence != null && !idOccurrence.isEmpty()) {
                    Intent intent = new Intent(MainMenu.this, GroupDetailActivity.class);
                    intent.putExtra("group_id", idOccurrence);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Aucune intervention disponible", Toast.LENGTH_SHORT).show();
                }
            },
            () -> {
                Intent intent = new Intent(this, SummaryInterventionPlan.class);
                startActivity(intent);
            },
            () -> {
                Intent intent = new Intent(this, EvaluateProfessionalsInvolved.class);
                intent.putExtra("option", 1);
                startActivity(intent);
            },
            () -> {
                Intent intent = new Intent(this, Badges.class);
                startActivity(intent);
            },
            () -> {
                Intent intent = new Intent(this, ListAbsence.class);
                startActivity(intent);
            },
            () -> {
                Intent intent = new Intent(this, SummaryOperation.class);
                intent.putExtra("option", 1);
                startActivity(intent);

            },
            () -> {
                Intent intent = new Intent(this, ExtendIntervPlan.class);
                startActivity(intent);
            },
            () -> {
                Intent intent = new Intent(this, SetupMyEnvironment.class);
                intent.putExtra("option", 1);
                startActivity(intent);
            },
            /*
            () -> {
                Intent intent = new Intent(this, Appointments.class);
                intent.putExtra("option", 1);
                startActivity(intent);
            },
            () -> {
                Intent intent = new Intent(this, MessageModule.class);
                intent.putExtra("option", 1);
                startActivity(intent);
            },
            () -> {
                if (ischeckInterventionChoice) {
                    Intent intent = new Intent(this, Planning.class);
                    intent.putExtra("option", 1);
                    startActivity(intent);
                } else {
                    buildNotification();
                }

            },
             */
            () -> {
                Intent intent = new Intent(this, AdministrativeDetails.class);
                intent.putExtra("option", 2);
                startActivity(intent);
            },
            () -> {
                Intent intent = new Intent(this, MainMenuPlus.class);
                intent.putExtra("option", 1);
                startActivity(intent);
            }
    };


    private APIRequest reqGetAccountInfos, reqFunnyMessage;
    private CustomOverlay customOverlay, HiUser_customOverlay, textWallet_customOverlay, boxProchainesInterventionsLayout_customOverlay;
    private CustomOverlay_Loading customOverlayLoading, HiUser_customOverlayLoading, textWallet_customOverlayLoading, boxProchainesInterventionsLayout_customOverlayLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Storage
        Storage.init(getApplicationContext());

        LibFunctions.handleBackNavigation(this, () -> {});

        setContentView(R.layout.main_menu);

        customOverlay = new CustomOverlay(this);
        customOverlayLoading = new CustomOverlay_Loading(customOverlay);
        customOverlayLoading.show();


        setupMainMenu();


    }

    /*
    private void triggerFireworks() {
        EmitterConfig emitterConfig = new Emitter(5L, TimeUnit.SECONDS).perSecond(50);
        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(400)
                .setSpeedBetween(1f, 10f)
                .timeToLive(4000L)
                .shapes(new Shape.Rectangle(0.2f))
                .sizes(new Size(12, 5f, 0.2f))
                .position(0.0, 0.0, 1.0, 0.0)
                .build();
        konfettiView.start(party);


    }

     */



    private void fetchCustNotesForUser() {
        reqGetUserNotes = new APIRequest("getCustNotesForUser");
        reqGetUserNotes.addToken();  // Ajouter le token d'authentification
        reqGetUserNotes.onSuccess(this::handleUserNotesSuccess);
        reqGetUserNotes.onFail(error -> {
            Toast.makeText(MainMenu.this, "Échec de la récupération des évaluations!", Toast.LENGTH_SHORT).show();
        });
        reqGetUserNotes.call();
    }

    private void handleUserNotesSuccess() {
        JSONObject response = reqGetUserNotes.getResponseJSON();

        try {
            JSONArray notesArray = response.getJSONArray("notes");

            // Si aucune note n'est trouvée, ne rien afficher
            if (notesArray.length() == 0) {
                nbPropositionsTextView.setText("0");
                return;
            }

            // Compter le nombre de notes et vérifier si `isnew` est à 0
            int noteCount = notesArray.length();
            boolean hasNew = false;

            for (int i = 0; i < notesArray.length(); i++) {
                JSONObject note = notesArray.getJSONObject(i);
                int isNew = note.getInt("isnew");

                if (isNew == 0) {
                    hasNew = true;
                }
            }

            // Afficher le nombre de notes
            nbPropositionsTextView.setText(String.valueOf(noteCount));

            // Si `isnew` est à 0 pour une note, afficher en rouge, sinon en gris
            if (hasNew) {
                nbPropositionsTextView.setTextColor(Color.RED);
            } else {
                nbPropositionsTextView.setTextColor(getColor(R.color.paleblue));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainMenu.this, "Erreur de parsing JSON!", Toast.LENGTH_SHORT).show();
        }
    }



    private void setupMainMenu() {
        //setContentView(R.layout.main_menu);
        // Your existing setup code for the main menu
        initializeMainMenuComponents();

        customOverlayLoading.hide();
    }

    private void initializeMainMenuComponents(){
        Log.d("MainMenu", "initializeMainMenuComponents called");

        // Create the notification channel
        createNotificationChannel();

        // Request notification and exact alarm permissions
        checkAndRequestPermissions();

        cardsIds = new int[]{
                R.id.boxProchainesInterventions,
                R.id.boxPlansInterventions,
                R.id.boxEvaluation,
                R.id.boxbadges,
                R.id.boxabsence,
                R.id.boxAides,
                R.id.boxFinPlanIntervention,
                R.id.boxParametreEnvironnement,
                //R.id.boxRendezVous,
                //R.id.boxMessage,
                //R.id.boxPlanning,
                R.id.boxAdministratif,
                R.id.boxPlus
        };

        for (int i = 0; i < cardsIds.length; i++) {
            cards[i] = findViewById(cardsIds[i]);

            final int currentIndex = i;

            cards[i].setOnClickListener(view -> cardsFunctions[currentIndex].run());
        }

        boxProchainesInterventionsLayout_customOverlay = new CustomOverlay(findViewById(R.id.boxProchainesInterventionsLayout));
        boxProchainesInterventionsLayout_customOverlayLoading = new CustomOverlay_Loading(boxProchainesInterventionsLayout_customOverlay);

        notification = findViewById(R.id.icon_notification);
        ischeckInterventionChoice = Storage.getBoolean("SummaryInterventionChoice", "checkbox");
        ischeckInterventionChoice = true; // Forcé pour cacher ça pour le moment
        if (!ischeckInterventionChoice) {
            notification.setVisibility(View.VISIBLE);
            notification.setOnClickListener(v -> buildNotification());
            planning = findViewById(R.id.boxPlanning);
            planning.setCardBackgroundColor(Color.GRAY);
        } else {
            notification.setVisibility(View.GONE);
        }


        HiUser = findViewById(R.id.hiUser);
        HiUser_customOverlay = new CustomOverlay(HiUser);
        HiUser_customOverlayLoading = new CustomOverlay_Loading(HiUser_customOverlay);

        HiUser.setOnClickListener(view -> {
            // Rediriger vers la page ProfileSettings
            Intent intent = new Intent(MainMenu.this, ProfileID.class);
            intent.putExtra("option", 1);
            startActivity(intent);
        });

        notification_inapp_container = findViewById(R.id.notification_inapp_container);
        notification_inapp_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotificationList();
            }
        });


        notification_inapp_counter_container = findViewById(R.id.notification_inapp_counter_container);
        notification_inapp_counter_text = findViewById(R.id.notification_inapp_counter_text);

        choiceQuest=findViewById(R.id.tmpChoiceQuests);
        choiceQuest.setOnClickListener(view -> {
            // TODO: Delete this shortcut
            /*
            Intent intent = new Intent(MainMenu.this, ChoiceQuest.class);
            intent.putExtra("choice_quest_question_id", ChoiceQuest.DEFAULT_QUESTION_ID);
            startActivity(intent);
            finish();

             */
            Toast.makeText(this, "Veuillez passer par la route des plans d'intervention", Toast.LENGTH_SHORT).show();
        });

        if (!Storage.isAuthTokenStored()) {
            Toast.makeText(MainMenu.this, "Aucun auth_token stocké, impossible de contacter le serveur", Toast.LENGTH_LONG).show();
            return;
        }
        // Get the email and display it in the HiUser TextView
        HiUser_customOverlayLoading.show();
        reqGetAccountInfos = new APIRequest("getAccountInfos");
        reqGetAccountInfos.onSuccess(this::onGetAccountInfosSuccess);
        reqGetAccountInfos.addToken();
        reqGetAccountInfos.call();


        textDateReprise=findViewById(R.id.textDateReprise2);

        boxabsence = findViewById(R.id.boxabsence);
        boxabsence.setVisibility(View.GONE);
        fetchAbsenceInfo();


        boxFinPlanIntervention= findViewById(R.id.boxFinPlanIntervention);// c'est ce cardview la qui sera affiché quand la date est à moins d'une semaine
        boxFinPlanIntervention.setVisibility(View.GONE);
        fetchLatestEndDate();


        inflater = LayoutInflater.from(this);
        imagehorlogeContainer = findViewById(R.id.imagehorloge_container);

        nameNextIntervenante=findViewById(R.id.userName);
        dateNextIntervenante=findViewById(R.id.dateIntervention);

        NmbreDaysLeft=findViewById(R.id.NmbreDaysLeft);
        dateEndIntervention=findViewById(R.id.dateEndIntervention);

        checkNextOccurrence();

        text_aides = findViewById(R.id.aides);
        final View boxAidesView = findViewById(R.id.boxAides);
        reqHasPlanning = new APIRequest("hasPlanning");
        reqHasPlanning.addToken();
        reqHasPlanning.onSuccess(() -> {
            JSONObject json = reqHasPlanning.getResponseJSON();
            int status = (json != null) ? json.optInt("status", -1) : -1;
            planningStatus = status;

            if (status == 2) {
                // Il y a au moins un plan actif => pas besoin d'afficher le card Aides
                boxAidesView.setVisibility(View.GONE);
            } else {
                boxAidesView.setVisibility(View.VISIBLE);
                if (status == -1) {
                    text_aides.setText("Pour configurer votre 1er plan");
                    buttonAides.setText("cliquez ici");
                }
                if (status == 0) {
                    text_aides.setText("PVous n'avez aucun plan en cours ou en configuration");
                    buttonAides.setText("Configurez un plan");
                }
                if (status == 1) {
                    text_aides.setText("Vous avez déjà un plan en cours de configuration");
                    buttonAides.setText("reprendre ou vous en étiez");
                }
            }

            if (status == -1) {
                noplanStatus = -1;
                updateRestrictedFeatures(true);   // grise + popup
            } else {
                noplanStatus = 0;                 // ou autre valeur ≠ -1
                updateRestrictedFeatures(false);  // restaure comportements normaux
            }
        });
        reqHasPlanning.onFail(err -> {
            // En cas d’échec réseau on ne masque pas (comportement safe)
            planningStatus = null;
        });
        reqHasPlanning.call();



        buttonAides = findViewById(R.id.buttonAides);
        buttonAides.setOnClickListener(v -> {
            // Si on a déjà le status, on route tout de suite
            if (planningStatus != null) {
                routeAccordingToPlanningStatus(planningStatus);
                return;
            }

            // Sinon, on fait un fallback: on appelle l’API à la volée
            APIRequest req = new APIRequest("hasPlanning");
            req.addToken();
            req.setTimeout(4000);
            req.onSuccess(() -> {
                JSONObject json = req.getResponseJSON();
                int status = (json != null) ? json.optInt("status", -1) : -1;
                planningStatus = status; // on mémorise
                routeAccordingToPlanningStatus(status);
            });
            req.onFail(error -> {
                Toast.makeText(MainMenu.this, "Impossible de vérifier vos plans. Réessayez.", Toast.LENGTH_SHORT).show();
            });
            req.call();
        });
        buttonFinPlanIntervention = findViewById(R.id.buttonFinPlanIntervention);
        buttonFinPlanIntervention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, ExtendIntervPlan.class);
                startActivity(intent);
            }
        });



        LinearLayout mainLayout = findViewById(R.id.mainLayout);

        /*
         konfettiView = findViewById(R.id.konfettiView);

        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerFireworks();
            }
        });

         */

        nbPropositionsTextView = findViewById(R.id.textView9);
        if (nbPropositionsTextView == null) {
            Log.d("MainMenu", "nbPropositionsTextView is null");
        }
        fetchCustNotesForUser();


        cricleredConv=findViewById(R.id.message_notif_iconred);
        textcircleConv=findViewById(R.id.message_notif_text);

        reqNumberNewConv= new APIRequest("countNewConversationsForCust");
        reqNumberNewConv.onSuccess(this::onGetNewConvSuccess);
        reqNumberNewConv.addToken();
        reqNumberNewConv.call();

        fetchEndDate();

         textWallet=findViewById(R.id.text_wallet);
         textWallet_customOverlay = new CustomOverlay(textWallet);
         textWallet_customOverlayLoading = new CustomOverlay_Loading(textWallet_customOverlay);
         icon_wallet=findViewById(R.id.icon_wallet);

        textWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, WalletClient.class);
                startActivity(intent);
            }
        });

        icon_wallet.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, WalletClient.class);
                startActivity(intent);
            }
        });

        fetchTokensAndDisplayTotal();

        // Fetch notifications in app and update the display accordingly
        reqGetNotificationsInapp = new APIRequest("getNotificationsInapp");
        reqGetNotificationsInapp.addToken();
        reqGetNotificationsInapp.setTimeout(3000);
        reqGetNotificationsInapp.onSuccess(this::onGetUnreadNotificationsInApp);
        reqGetNotificationsInapp.call();
    }

    private void onGetNewConvSuccess(){
        JSONObject json = reqNumberNewConv.getResponseJSON();
        try {
            Integer numberNewConv = json.getInt("new_conversations");
            if (numberNewConv>0){
                cricleredConv.setVisibility(View.VISIBLE);
                textcircleConv.setVisibility(View.VISIBLE);
                textcircleConv.setText(numberNewConv.toString());
            }
            else {
                cricleredConv.setVisibility(View.INVISIBLE);
                textcircleConv.setVisibility(View.INVISIBLE);
            }

        } catch (JSONException | DateTimeParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse next occurrence: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void fetchAbsenceInfo() {
        reqGetAbsenceInfo = new APIRequest("getAbsenceInfo");
        reqGetAbsenceInfo.addToken();
        reqGetAbsenceInfo.onSuccess(this::updateAbsenceVisibility);
        reqGetAbsenceInfo.onFail(error -> {
            Toast.makeText(MainMenu.this, "Error fetching absence info", Toast.LENGTH_SHORT).show();
        });
        reqGetAbsenceInfo.call();
    }

    private void routeAccordingToPlanningStatus(int status) {
        noplanStatus=0;
        switch (status) {
            case -1:
                // Aucun plan
                noplanStatus=-1;
                Intent iNeg1 = new Intent(MainMenu.this, ChoiceQuest_Cust.class);
                iNeg1.putExtra("choice_quest_question_id", ChoiceQuest_Cust.DEFAULT_QUESTION_ID);
                startActivity(iNeg1);
                break;

            case 0:
                // Aucun plan actif NI en configuration (branche séparée de -1)
                Intent i0 = new Intent(MainMenu.this, ChoiceQuest_Cust.class);
                i0.putExtra("choice_quest_question_id", ChoiceQuest_Cust.DEFAULT_QUESTION_ID);
                startActivity(i0);
                break;

            case 1:
                // Au moins un plan en configuration, aucun actif
                Intent i1 = new Intent(MainMenu.this, SummaryOperation.class);
                startActivity(i1);
                break;

            case 2:
                // Au moins un plan actif => le card Aides est déjà caché
                // (par sécurité, on peut éviter d’ouvrir quoi que ce soit)
                Toast.makeText(this, "Un plan actif est déjà en cours.", Toast.LENGTH_SHORT).show();
                break;

            default:
                // fallback prudent
                Toast.makeText(this, "Statut planning inconnu.", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private void updateAbsenceVisibility() {
        JSONObject json = reqGetAbsenceInfo.getResponseJSON();
        try {
            String absenceType = json.getString("type");
            if ("absence".equals(absenceType)) {
                String startDateStr = json.getString("start_date");
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                LocalDate startDate = LocalDate.parse(startDateStr, formatter);
                LocalDate today = LocalDate.now();

                if (startDate != null && !startDate.isAfter(today)) {
                    String endDateStr = json.optString("end_date");
                    if (endDateStr != null && !endDateStr.isEmpty()) {
                        LocalDate endDate = LocalDate.parse(endDateStr, formatter);
                        textDateReprise.setText("avant le " + endDate);
                    } else {
                        textDateReprise.setText("durée indéterminée");
                    }
                    boxabsence.setVisibility(View.VISIBLE);
                } else {
                    boxabsence.setVisibility(View.GONE);
                }
            } else {
                boxabsence.setVisibility(View.GONE);
            }
        } catch (JSONException | DateTimeParseException e) {
            boxabsence.setVisibility(View.GONE);
        }
    }



    private void onGetAccountInfosSuccess() {
        String firstname = reqGetAccountInfos.getData("firstname");
        HiUser.setText(firstname);
        HiUser_customOverlayLoading.hide();
    }


    private void scheduleNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivityForResult(intent, REQUEST_CODE_SCHEDULE_EXACT_ALARM);
            return;
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = System.currentTimeMillis() + 5000; // Trigger after 5 seconds for testing
        long intervalTime = 5*60 * 1000; // Repeat every minute

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }

        // For repeating alarms
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, intervalTime, pendingIntent);
    }

    private void buildNotification() {
        // Construire le message à afficher
        StringBuilder message = new StringBuilder("Veuillez valider vos parametre d'intervention pour pouvoir acceder au planning.\n\n");

        // Créer et afficher l'AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this)
                .setTitle("Attention !")
                .setMessage(message.toString())
                .setNeutralButton("OK", (dialog, which) -> {
                    // Ne fais rien
                });

        // Ajouter une condition pour le bouton "Transmettre les documents"
        builder.setPositiveButton("Valider mes informations", (dialog, which) -> SummaryInterventionChoice());

        builder.show();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
                return;  // Exit the method, the result will be handled in onRequestPermissionsResult
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, REQUEST_CODE_SCHEDULE_EXACT_ALARM);
                return;  // Exit the method, the result will be handled in onActivityResult
            }
        }

        // If all permissions are granted, proceed with scheduling notifications
        scheduleNotification();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCHEDULE_EXACT_ALARM) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "Exact alarm permission granted", Toast.LENGTH_SHORT).show();
                    // Schedule the notification
                    scheduleNotification();
                } else {
                    Toast.makeText(this, "Exact alarm permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                // Check if we also need the exact alarm permission
                checkAndRequestPermissions();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SummaryInterventionChoice() {
        Intent intent = new Intent(this, SummaryInterventionChoice.class);
        intent.putExtra("option", 2); // Indicate to change the back destination to the main menu.
        startActivity(intent);
    }

    private void sendNotification() {
        Intent intent = new Intent(this, MainMenu.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("Hello World")
                .setContentText("This is a test notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Channel for default notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void fetchLatestEndDate() {
        reqGetLatestEndDate = new APIRequest("getLatestEndDate");
        reqGetLatestEndDate.addToken();
        reqGetLatestEndDate.onSuccess(this::updateFinPlanInterventionVisibility);
        reqGetLatestEndDate.onFail(error -> {
            Toast.makeText(MainMenu.this, "Error fetching latest end date", Toast.LENGTH_SHORT).show();
        });
        reqGetLatestEndDate.call();
    }

    private void updateFinPlanInterventionVisibility() {
        JSONObject json = reqGetLatestEndDate.getResponseJSON();
        try {
            if (json.has("date_end") && !json.isNull("date_end")) {
                String endDateStr = json.getString("date_end");
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                LocalDate endDate = LocalDate.parse(endDateStr, formatter);
                LocalDate today = LocalDate.now();

                if (endDate != null && !endDate.isBefore(today) && !endDate.isAfter(today.plusWeeks(1))) {
                    boxFinPlanIntervention.setVisibility(View.VISIBLE);
                } else {
                    boxFinPlanIntervention.setVisibility(View.GONE);
                }
            } else {
                boxFinPlanIntervention.setVisibility(View.GONE);
            }
        } catch (JSONException | DateTimeParseException e) {
            boxFinPlanIntervention.setVisibility(View.GONE);
        }
    }

    private void checkNextOccurrence() {
        boxProchainesInterventionsLayout_customOverlayLoading.show();

        reqGetNextOccurrence = new APIRequest("getNextGroupOccurrenceCust");
        reqGetNextOccurrence.addToken();
        reqGetNextOccurrence.onSuccess(this::handleNextOccurrenceCheck);
        reqGetNextOccurrence.onFail(error -> {
            Toast.makeText(MainMenu.this, "Error checking next occurrence", Toast.LENGTH_SHORT).show();
            // Optionally, handle the error
        });
        reqGetNextOccurrence.call();
    }

    private void handleNextOccurrenceCheck() {
        JSONObject json = reqGetNextOccurrence.getResponseJSON();
        try {
            if (json.has("next_occurrence") && !json.isNull("next_occurrence")) {
                String nextOccurrenceStr = json.getString("next_occurrence");
                String lastname          = json.optString("lastname", "");
                String firstname         = json.optString("firstname", "");
                String idGroup           = json.optString("id_group", null);

                JSONArray svcArr = json.optJSONArray("service_types");
                List<Integer> serviceTypes = new ArrayList<>();
                if (svcArr != null) {
                    for (int i = 0; i < svcArr.length(); i++) {
                        serviceTypes.add(svcArr.getInt(i));
                    }
                }

                LocalDateTime dt = LocalDateTime.parse(
                        nextOccurrenceStr,
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );
                String formatted = dt.format(
                        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.FRANCE)
                );

                if (lastname.isEmpty() && firstname.isEmpty()) {
                    nameNextIntervenante.setText("Intervention non pourvue");
                } else {
                    StringBuilder sb = new StringBuilder();
                    int sex = json.optInt("sexe", -1);
                    if (sex == 1) sb.append("Mme ");
                    else if (sex == 0) sb.append("M. ");
                    sb.append(lastname.toUpperCase())
                            .append(" ")
                            .append(firstname);
                    nameNextIntervenante.setText(sb.toString());
                }

                dateNextIntervenante.setText(formatted);

                imagehorlogeContainer.setVisibility(View.VISIBLE);
                imagehorlogeContainer.removeAllViews();
                for (int svc : serviceTypes) {
                    View icon = inflater.inflate(
                            eventLayoutForTask(svc),
                            imagehorlogeContainer,
                            false
                    );
                    imagehorlogeContainer.addView(icon);
                }

                idOccurrence = idGroup;
            } else {
                nameNextIntervenante.setText("Aucune intervention prévue");
                dateNextIntervenante.setText("Créez un nouveau plan");
                imagehorlogeContainer.removeAllViews();
                imagehorlogeContainer.setVisibility(View.GONE);
                idOccurrence = null;
            }

            boxProchainesInterventionsLayout_customOverlayLoading.hide();
        } catch (JSONException | DateTimeParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du parsing", Toast.LENGTH_SHORT).show();
        }
    }


    private int eventLayoutForTask(int type) {
        switch (type) {
            case 1:  return R.layout.event_rectangle_clothes;
            case 2:  return R.layout.event_rectangle_shower;
            case 3:  return R.layout.event_rectangle_change;
            case 4:  return R.layout.event_rectangle_bed;
            case 5:  return R.layout.event_rectangle_cooking;
            case 6:  return R.layout.event_rectangle_pill;
            case 7:  return R.layout.event_rectangle_shopping;
            case 8:  return R.layout.event_rectangle_cleaning;
            case 9:  return R.layout.event_rectangle_brain;
            case 10: return R.layout.event_rectangle_walking;
            default: return R.layout.event_rectangle_unknown;
        }
    }


    private void fetchEndDate() {
        reqGetEndDate = new APIRequest("endTimePlanningCust");
        reqGetEndDate.addToken();
        reqGetEndDate.onSuccess(this::handleEndDateCheck);
        reqGetEndDate.onFail(error -> {
            Toast.makeText(MainMenu.this, "Error checking end date", Toast.LENGTH_SHORT).show();
            // Optionally, handle the error
        });
        reqGetEndDate.call();
    }
    private void handleEndDateCheck() {
        JSONObject json = reqGetEndDate.getResponseJSON();
        try {
            Boolean found = json.getBoolean("found");
            if(found) {
                if (json.has("date_end") && !json.isNull("date_end")) {
                    String endDateStr = json.getString("date_end");

                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);

                    DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    String endDateFormatted = endDate.format(displayFormatter);

                    dateEndIntervention.setText("La date de fin du plan est le : " + endDateFormatted);

                    LocalDateTime today = LocalDateTime.now();
                    long daysLeft = ChronoUnit.DAYS.between(today, endDate);
                    NmbreDaysLeft.setText("il reste " + daysLeft + " jours pour votre plan");
                } else {
                    dateEndIntervention.setText("Plan à durée indéterminée");
                    NmbreDaysLeft.setText("Vous n'avez pas besoin de renouveler");
                }
            }
            else {
                dateEndIntervention.setText("Vous n'avez aucun plan actif");
                NmbreDaysLeft.setText("Veuillez en recréer un pour suivre vos interventions");
            }
        } catch (JSONException | DateTimeParseException e) {
            throw new RuntimeException(e);
        }
    }
    private void fetchTokensAndDisplayTotal() {
        textWallet_customOverlayLoading.show();

        APIRequest reqGetTokens = new APIRequest("getTokens");
        reqGetTokens.addToken();

        reqGetTokens.onSuccess(() -> {
            JSONObject response = reqGetTokens.getResponseJSON();
            int totalTokens = 0;

            try {
                JSONArray tokens = response.getJSONArray("tokens");
                SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH);
                Date today = new Date();

                for (int i = 0; i < tokens.length(); i++) {
                    JSONObject token = tokens.getJSONObject(i);
                    String origin = token.getString("origin");
                    int amount = token.getInt("amout");
                    String startStr = token.getString("validityStart");
                    String endStr   = token.isNull("validityEnd") ? null : token.getString("validityEnd");

                    // On ne compte que les jetons "client"
                    if (!"client".equals(origin)) continue;

                    Date startDate = inputFormat.parse(startStr);
                    Date endDate   = endStr != null ? inputFormat.parse(endStr) : null;

                    boolean isActive = today.after(startDate) && (endDate == null || today.before(endDate));
                    if (isActive) {
                        totalTokens += amount;
                    }
                }

                textWallet.setText(totalTokens + " Tokens");
            } catch (JSONException|ParseException e) {
                e.printStackTrace();
                Toast.makeText(MainMenu.this,
                        "Erreur lors de l'analyse des données de jetons", Toast.LENGTH_SHORT).show();
            } finally {
                textWallet_customOverlayLoading.hide();
            }
        });

        reqGetTokens.onFail(() ->
                Toast.makeText(MainMenu.this,
                        "Erreur de récupération des jetons", Toast.LENGTH_SHORT).show()
        );
        reqGetTokens.call();
    }

    private void onGetUnreadNotificationsInApp(){
        JSONObject json = reqGetNotificationsInapp.getResponseJSONObject();
        try {
            JSONArray jsonNotifications = json.getJSONArray("notifications");
            int unreadNotifications = 0;
            for (int i = 0; i < jsonNotifications.length(); i++) {
                JSONObject jsonNotif = jsonNotifications.getJSONObject(i);
                if (!jsonNotif.has("status")) {
                    continue;
                }
                int status = jsonNotif.getInt("status");
                if (status == 1) {
                    unreadNotifications++;
                }
            }

            if (unreadNotifications>0){
                notification_inapp_counter_container.setVisibility(View.VISIBLE);
                notification_inapp_counter_text.setText(((Integer) unreadNotifications).toString());
            }
            else {
                notification_inapp_counter_container.setVisibility(View.INVISIBLE);
            }

        } catch (JSONException | DateTimeParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "onGetUnreadNotificationsInApp: Failed to parse inapp notifications: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openMainMenu() {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
        finish();
    }

    private void openNotificationList() {
        Intent intent = new Intent(this, NotificationList_Cust.class);
        startActivity(intent);
        finish();
    }


    private void showNoPlanDialog() {
        // Si pour une raison quelconque on sait déjà qu'on a un plan, on passe directement à l'ancienne popup
        if (noplanStatus != null && noplanStatus != -1) {
            showFirstPlanDialog();
            return;
        }

        // États de chargement / validation
        final boolean[] docsLoaded = {false};
        final boolean[] qrcLoaded  = {false};
        final boolean[] addrLoaded = {false};

        final boolean[] docsOk = {false};
        final boolean[] qrcOk  = {false};
        final boolean[] addrOk = {false};

        final JSONObject[] docsJsonHolder = {null};

        Runnable tryShowDialog = () -> {
            if (!docsLoaded[0] || !qrcLoaded[0] || !addrLoaded[0]) {
                return; // on attend que les 3 réponses soient là
            }

            // Tout est OK → ancienne popup “créer 1er plan”
            if (docsOk[0] && qrcOk[0] && addrOk[0]) {
                showFirstPlanDialog();
                return;
            }

            // Construire le message détaillé
            StringBuilder msg = new StringBuilder();
            msg.append("Avant d’accéder à cette fonctionnalité et de créer votre premier plan d’intervention, il reste des éléments à compléter :\n\n");

            if (!docsOk[0]) {
                msg.append(buildDocsDetails(docsJsonHolder[0])).append("\n");
            }
            if (!qrcOk[0]) {
                msg.append("• QR Code du domicile : non généré.\n");
            }
            if (!addrOk[0]) {
                msg.append("• Adresse / accès au domicile : non renseigné.\n");
            }

            // Inflate du layout custom
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_no_plan_prereqs, null);

            TextView tvMsg     = dialogView.findViewById(R.id.text_prereqs_message);
            View cardDocs      = dialogView.findViewById(R.id.card_docs);
            View cardQr        = dialogView.findViewById(R.id.card_qr);
            View cardAddr      = dialogView.findViewById(R.id.card_addr);
            View cardLater     = dialogView.findViewById(R.id.card_later);

            tvMsg.setText(msg.toString().trim());

            // Construire la boîte de dialogue
            androidx.appcompat.app.AlertDialog dlg =
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Pré-requis avant le 1er plan")
                            .setView(dialogView)
                            .create();

            // Documents
            if (!docsOk[0]) {
                cardDocs.setVisibility(View.VISIBLE);
                cardDocs.setOnClickListener(v -> {
                    dlg.dismiss();
                    Intent i = new Intent(MainMenu.this, TransmissionDocuments.class);
                    startActivity(i);
                });
            } else {
                cardDocs.setVisibility(View.GONE);
            }

            // QR Code
            if (!qrcOk[0]) {
                cardQr.setVisibility(View.VISIBLE);
                cardQr.setOnClickListener(v -> {
                    dlg.dismiss();
                    Intent i = new Intent(MainMenu.this, QRC.class);
                    startActivity(i);
                });
            } else {
                cardQr.setVisibility(View.GONE);
            }

            // Adresse
            if (!addrOk[0]) {
                cardAddr.setVisibility(View.VISIBLE);
                cardAddr.setOnClickListener(v -> {
                    dlg.dismiss();
                    Intent i = new Intent(MainMenu.this, AccessPlaceIntervention.class);
                    startActivity(i);
                });
            } else {
                cardAddr.setVisibility(View.GONE);
            }

            // Bouton “Plus tard”
            cardLater.setOnClickListener(v -> dlg.dismiss());

            dlg.show();
        };

        // --- 1) getDocumentStatuses ---
        APIRequest reqDocs = new APIRequest("getDocumentStatuses");
        reqDocs.addToken();
        reqDocs.setTimeout(5000);
        reqDocs.onSuccess(() -> {
            JSONObject json = reqDocs.getResponseJSON();
            docsJsonHolder[0] = json;
            docsOk[0] = areAllDocsValid(json);
            docsLoaded[0] = true;
            tryShowDialog.run();
        });
        reqDocs.onFail(error -> {
            docsLoaded[0] = true;
            docsOk[0] = false;
            tryShowDialog.run();
        });
        reqDocs.call();

        // --- 2) checkQRC ---
        APIRequest reqQrc = new APIRequest("checkQRC");
        reqQrc.addToken();
        reqQrc.setTimeout(5000);
        reqQrc.onSuccess(() -> {
            JSONObject json = reqQrc.getResponseJSON();
            int has = (json != null) ? json.optInt("hasqrcode", 0) : 0;
            qrcOk[0] = (has == 1);
            qrcLoaded[0] = true;
            tryShowDialog.run();
        });
        reqQrc.onFail(error -> {
            qrcLoaded[0] = true;
            qrcOk[0] = false;
            tryShowDialog.run();
        });
        reqQrc.call();

        // --- 3) checkAdresse ---
        APIRequest reqAddr = new APIRequest("checkAdresse");
        reqAddr.addToken();
        reqAddr.setTimeout(5000);
        reqAddr.onSuccess(() -> {
            JSONObject json = reqAddr.getResponseJSON();
            int exist = (json != null) ? json.optInt("exist", 0) : 0;
            addrOk[0] = (exist == 1);
            addrLoaded[0] = true;
            tryShowDialog.run();
        });
        reqAddr.onFail(error -> {
            addrLoaded[0] = true;
            addrOk[0] = false;
            tryShowDialog.run();
        });
        reqAddr.call();
    }

    private void applyNoPlanRestrictionsToCard(CardView card) {
        if (card == null) return;
        // Look métallique simple
        card.setCardBackgroundColor(Color.parseColor("#BDBDBD")); // gris “metal”
        card.setCardElevation(2f);
        card.setAlpha(0.85f);

        // Remplace le clic par la popup
        card.setOnClickListener(v -> showNoPlanDialog());
    }
    /** Retire le style grisé et ré-associe le onClick original depuis cardsFunctions[] */
    private void removeRestrictionsFromCard(CardView card, int functionIndex) {
        if (card == null) return;
        // Restaure le look (adapter si tu as une couleur par défaut)
        card.setCardBackgroundColor(getColor(android.R.color.white));
        card.setAlpha(1f);

        // Réassocie le handler original
        card.setOnClickListener(v -> cardsFunctions[functionIndex].run());
    }
    /** Active/désactive les restrictions en bloc */
    private void updateRestrictedFeatures(boolean noPlan) {
        // Trouve l’index de chaque card dans cards[] pour réassocier le bon Runnable au besoin
        // Indices selon ton tableau cardsIds défini plus haut :
        // cardsIds = { boxProchainesInterventions(0), boxPlansInterventions(1), boxEvaluation(2), boxbadges(3), ..., boxParametreEnvironnement(7), ..., boxAdministratif(9), boxPlus(10) }
        // => boxEvaluation = index 2, boxbadges = index 3, boxParametreEnvironnement = index 7

        CardView cEval  = findViewById(R.id.boxEvaluation);
        CardView cBadge = findViewById(R.id.boxbadges);
        CardView cEnv   = findViewById(R.id.boxParametreEnvironnement);

        if (noPlan) {
            applyNoPlanRestrictionsToCard(cEval);
            applyNoPlanRestrictionsToCard(cBadge);
            applyNoPlanRestrictionsToCard(cEnv);
        } else {
            removeRestrictionsFromCard(cEval, 2);
            removeRestrictionsFromCard(cBadge, 3);
            removeRestrictionsFromCard(cEnv, 7);
        }
    }

    private boolean areAllDocsValid(JSONObject json) {
        if (json == null) return false;

        int[] statuses = new int[] {
                json.optInt("IDDocuments_status",   -1),
                json.optInt("Procuration_status",   -1),
                json.optInt("PlanFinancement_status", -1),
                json.optInt("CVITransmission_status", -1),
                json.optInt("PhotoProfil_status",   -1)
        };

        for (int st : statuses) {
            if (st != 1) {
                return false;   // au moins un doc non validé
            }
        }
        return true;
    }

    private String statusToText(int st) {
        switch (st) {
            case -1: return "non transmis";
            case 0:  return "en cours de validation";
            case 1:  return "validé";
            case 2:  return "refusé";
            default: return "inconnu";
        }
    }

    /** Construit les lignes détaillées pour les docs non OK */
    private String buildDocsDetails(JSONObject json) {
        if (json == null) {
            return "- Documents : impossible de vérifier les statuts.\n";
        }

        StringBuilder sb = new StringBuilder();

        int idSt      = json.optInt("IDDocuments_status",   -1);
        int procSt    = json.optInt("Procuration_status",   -1);
        int finSt     = json.optInt("PlanFinancement_status", -1);
        int cviSt     = json.optInt("CVITransmission_status", -1);
        int photoSt   = json.optInt("PhotoProfil_status",   -1);

        if (idSt != 1) {
            sb.append("• Pièce d'identité / titre de séjour : ").append(statusToText(idSt)).append("\n");
        }
        if (procSt != 1) {
            sb.append("• Procuration : ").append(statusToText(procSt)).append("\n");
        }
        if (finSt != 1) {
            sb.append("• Plan de financement : ").append(statusToText(finSt)).append("\n");
        }
        if (cviSt != 1) {
            sb.append("• Carte Vitale / CVI : ").append(statusToText(cviSt)).append("\n");
        }
        if (photoSt != 1) {
            sb.append("• Photo de profil : ").append(statusToText(photoSt)).append("\n");
        }

        if (sb.length() == 0) {
            // Normalement, on ne passe ici que si au moins un n'est pas 1,
            // mais par sécurité :
            sb.append("• Documents : au moins un document reste à finaliser.\n");
        }

        return sb.toString();
    }
    private void showFirstPlanDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Fonctionnalité indisponible")
                .setMessage("Pour accéder à cette fonctionnalité, vous devez d’abord créer votre premier plan.\n\nSouhaitez-vous le créer maintenant ?")
                .setNegativeButton("Plus tard", (d, w) -> { /* on reste sur le MainMenu */ })
                .setPositiveButton("Créer maintenant", (d, w) -> {
                    Intent intent = new Intent(MainMenu.this, ChoiceQuest_Cust.class);
                    intent.putExtra("choice_quest_question_id", ChoiceQuest_Cust.DEFAULT_QUESTION_ID);
                    startActivity(intent);
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop API requests when the Activity is finished, to prevent doing requests for nothing
        if (reqCheckActivePlanning != null)
            reqCheckActivePlanning.cancel(true);

        if (reqGetAbsenceInfo != null)
            reqGetAbsenceInfo.cancel(true);

        if (reqGetLatestEndDate != null)
            reqGetLatestEndDate.cancel(true);

        if (reqGetUserNotes != null)
            reqGetUserNotes.cancel(true);

        if (reqGetNextOccurrence != null)
            reqGetNextOccurrence.cancel(true);

        if (reqGetProposals != null)
            reqGetProposals.cancel(true);

        if (reqNumberNewConv != null)
            reqNumberNewConv.cancel(true);

        if (reqGetAccountInfos != null)
            reqGetAccountInfos.cancel(true);

        if (reqGetNotificationsInapp != null)
            reqGetNotificationsInapp.cancel(true);
    }
}
