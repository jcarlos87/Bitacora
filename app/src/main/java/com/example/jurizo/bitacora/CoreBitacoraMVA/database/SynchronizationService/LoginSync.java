package com.example.jurizo.bitacora.CoreBitacoraMVA.database.SynchronizationService;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.jurizo.bitacora.CoreBitacoraMVA.controllers.AnswersSegmentController;
import com.example.jurizo.bitacora.CoreBitacoraMVA.controllers.DepartmentController;
import com.example.jurizo.bitacora.CoreBitacoraMVA.controllers.EmployeeController;
import com.example.jurizo.bitacora.CoreBitacoraMVA.controllers.LogsController;
import com.example.jurizo.bitacora.CoreBitacoraMVA.controllers.OfficesController;
import com.example.jurizo.bitacora.CoreBitacoraMVA.controllers.QuestionSegmentController;
import com.example.jurizo.bitacora.CoreBitacoraMVA.controllers.SegmentController;
import com.example.jurizo.bitacora.CoreBitacoraMVA.controllers.SessionController;
import com.example.jurizo.bitacora.CoreBitacoraMVA.controllers.UserController;
import com.example.jurizo.bitacora.CoreBitacoraMVA.database.DBHelper;
import com.example.jurizo.bitacora.CoreBitacoraMVA.database.SessionManagement;
import com.example.jurizo.bitacora.CoreBitacoraMVA.models.Employee;
import com.example.jurizo.bitacora.CoreBitacoraMVA.models.Session;
import com.example.jurizo.bitacora.CoreBitacoraMVA.models.User;
import com.example.jurizo.bitacora.PrincipalActivity;

/**
 * Created by Carlos Rizo on 26/06/2017.
 */

public class LoginSync extends AsyncTask<String, String, User> {
    private String TAG = "SyncLogin";
    private String TAGCLass = getClass().getSimpleName();
    private Context context;
    private ProgressDialog progressDialog;

    /*private static String hostname = ConfigServerConnection.getHostname();
    private static String port = ConfigServerConnection.getPort();
    private static String pathSyncFiles = ConfigServerConnection.getPathSyncFiles();

    private DBHelper dbHelper;*/
    private AlertDialog alertDialog;

    public LoginSync(Context context) {
        this.context = context;
    }

    @Override
    protected User doInBackground(String... params) {
        User usr = null;
        try {
            String username = params[0];
            String password = params[1];
            String access_type = params[1];
            String access_system = params[2];
            String ip_address = params[3];
            String serial_number = params[4];
            String imei = params[5];
            String sim_card_number = params[6];

            progressDialog.setCancelable(true);
            publishProgress("Validando credenciales ingresadas..");
            UserController usrController = new UserController(context);
            //Primero validamos si ya se encuentra registrado
            usr = usrController.UserValidateOffLine(username, password);
            if (usr == null) {
                usr = usrController.UserValidateOnLine(username, password, access_type, access_system, ip_address, serial_number, imei, sim_card_number);
                if (usr != null) {
                    boolean session_is_valid = false;
                    boolean load_employees = false;
                    boolean load_departments = false;
                    boolean load_segments = false;
                    boolean load_answers = false;
                    boolean load_questions = false;
                    boolean load_offices = false;

                    publishProgress("Validando sesión activa..");
                    SessionController sessionController = new SessionController(context);
                    Session session = sessionController.get_Final_Session(usr.getId());

                    publishProgress("Validando detalles del usuario");
                    EmployeeController employeeController = new EmployeeController(context);
                    Employee empLogeado = employeeController.getEmployeeLoginByServer(usr.getEmployee_id());
                    publishProgress("Descargando lista de empleados asignados");
                    load_employees = employeeController.Download_Update_Employee(usr.getUsername(), empLogeado);

                    publishProgress("Descargando catalogos principales, (Departamentos)...");
                    DepartmentController departmentController = new DepartmentController(context);
                    load_departments = departmentController.Download_Update_Department(usr.getUsername());

                    publishProgress("Descargando catalogos principales, (Segmentos)...");
                    SegmentController segmentController = new SegmentController(context);
                    load_segments = segmentController.Download_Update_Segments(usr.getUsername());

                    publishProgress("Descargando catalogos principales, (Preguntas)...");
                    QuestionSegmentController questionsegmentController = new QuestionSegmentController(context);
                    load_questions = questionsegmentController.Download_Update_Questions(usr.getUsername());

                    publishProgress("Descargando catalogos principales, (Respuestas)...");
                    AnswersSegmentController answersSegmentController = new AnswersSegmentController(context);
                    load_answers = answersSegmentController.Download_Update_Answers(usr.getUsername());

                    publishProgress("Descargando catalogos principales, (Oficinas)...");
                    OfficesController officesController = new OfficesController(context);
                    load_offices = officesController.Download_Update_Offices(usr.getUsername());

                    publishProgress("Descargando las visitas generadas");

                    if(session_is_valid == true && load_employees == true && load_departments == true &&
                            load_segments == true && load_questions == true &&
                            load_answers == true && load_offices == true){

                    }else{
                        usr.setStatus_user_id(5);
                        LogsController.LogError(TAG, TAGCLass, "Fallo la carga de la información", context);
                    }

                /*publishProgress("Actualizando información de usuarios");
                List<EntityUser> users = new ArrayList<>();
                users.add(user);
                int consecutivo = 0;
                while (consecutivo < users.size()) {
                    int usrId = users.get(consecutivo).getId();
                    List<EntityUser> usrAsignated = getUserAsignated(usrId);
                    if (usrAsignated != null && usrAsignated.size() > 0) {
                        users.addAll(usrAsignated);
                    }
                    consecutivo++;
                }
                DAO_Users daoUsers = new DAO_Users(context);
                daoUsers.insertUsers(users);


                publishProgress("Descargando visitas guardas en sistema");
                List<EntityVisita> visitas = getVisitasServer(ConfigServerConnection.getURLVisitas(), users);
                if (visitas != null && visitas.size() > 0) {
                    publishProgress("Procesando visitas..");
                    DAO_Visits dao_visits = new DAO_Visits(context);
                    dao_visits.insertVisitas(visitas);
                }*/
                }


                return usr;
            }
        } catch (Exception ex) {
            LogsController.LogError(TAG, TAGCLass, ex.getMessage(), context);
            ex.printStackTrace();
        }
        return usr;
    }


    @Override
    protected void onPreExecute() {
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Login Status");


        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Inicio de sesión");
        progressDialog.setMessage("Conectando con el servidor");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(User user) {
        super.onPostExecute(user);
        progressDialog.cancel();

        if (user != null) {
            switch (user.getStatus_user_id()) {
                case 0:
                    alertDialog.setMessage("Usuario bloqueado");
                    alertDialog.show();
                    break;
                case 1:
                    SessionManagement session = new SessionManagement(context);
                    session.createLoginSession(String.valueOf(user.getId()), String.valueOf(user.getId()), user.getUsername());
                    Intent intent = new Intent(context, PrincipalActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                case 5:
                    alertDialog.setMessage("Fallo la descarga de la información, por favor intentalo nuevamente");
                    alertDialog.show();
                    break;
            }
        } else {
            //alertDialog.setMessage("Usuario o contraseña incorrecta");
            //alertDialog.show();
            Toast.makeText(context, "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        progressDialog.setMessage(values[0]);
    }
}
