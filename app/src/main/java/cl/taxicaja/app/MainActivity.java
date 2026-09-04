package cl.taxicaja.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    DB db; LinearLayout root; TextView amountView, totalsView, identityView; String amount=""; long driverId=0,vehicleId=0; String driverName="Sin conductor", vehicleName="Sin móvil"; SharedPreferences prefs;
    int green=Color.rgb(22,163,74), red=Color.rgb(220,38,38), dark=Color.rgb(17,24,39), gray=Color.rgb(243,244,246);

    @Override public void onCreate(Bundle b){ super.onCreate(b); db=new DB(this); prefs=getSharedPreferences("cfg",MODE_PRIVATE); driverId=prefs.getLong("driver",0);vehicleId=prefs.getLong("vehicle",0); build(); }
    TextView tv(String s,int sp,boolean bold){ TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(dark);v.setPadding(12,8,12,8);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v; }
    TextView btn(String s){
        // Usamos TextView como botón para evitar el estilo interno de Button de algunos
        // teléfonos Samsung/Android 16, que puede recortar u ocultar las etiquetas.
        TextView b=new TextView(this);
        b.setText(s);
        b.setTextSize(17);
        b.setTextColor(dark);
        b.setGravity(Gravity.CENTER);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        b.setIncludeFontPadding(true);
        b.setPadding(8,8,8,8);
        b.setClickable(true);
        b.setFocusable(true);
        b.setSingleLine(true);
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.rgb(229,231,235));
        bg.setCornerRadius(10);
        b.setBackground(new InsetDrawable(bg,4,4,4,4));
        return b;
    }
    void build(){
        ScrollView sc=new ScrollView(this); root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(14,14,14,20);sc.addView(root);setContentView(sc);
        TextView title=tv("🚕 TAXI CAJA",24,true); title.setTextColor(Color.WHITE);title.setBackgroundColor(dark);title.setGravity(Gravity.CENTER);title.setPadding(10,18,10,18);root.addView(title);
        identityView=tv("",15,true);identityView.setGravity(Gravity.CENTER);root.addView(identityView);updateIdentity();
        totalsView=tv("",21,true);totalsView.setGravity(Gravity.CENTER);totalsView.setBackgroundColor(gray);totalsView.setPadding(10,16,10,16);root.addView(totalsView);refreshTotals();
        LinearLayout amountRow=new LinearLayout(this);amountRow.setOrientation(LinearLayout.HORIZONTAL);
        amountView=tv("$ 0",36,true);amountView.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);amountView.setBackgroundColor(Color.WHITE);amountView.setPadding(12,18,12,18);amountRow.addView(amountView,new LinearLayout.LayoutParams(0,82,1));
        TextView back=btn("⌫");back.setTextSize(26);back.setContentDescription("Borrar último dígito");back.setOnClickListener(v->backspace());amountRow.addView(back,new LinearLayout.LayoutParams(86,82));root.addView(amountRow);

        LinearLayout quick=new LinearLayout(this);quick.setOrientation(LinearLayout.HORIZONTAL);int[] q={prefs.getInt("q1",2000),prefs.getInt("q2",3000),prefs.getInt("q3",5000)};for(int n:q){TextView x=btn("$"+fmt(n));x.setTextSize(16);x.setOnClickListener(v->{amount=String.valueOf(n);showAmount();});quick.addView(x,new LinearLayout.LayoutParams(0,62,1));}root.addView(quick);
        String[][] keys={{"7","8","9"},{"4","5","6"},{"1","2","3"},{"C","0","000"}};for(String[] row:keys){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);for(String k:row){TextView x=btn(k);x.setTextSize(28);x.setTextColor(Color.BLACK);x.setGravity(Gravity.CENTER);x.setOnClickListener(v->key(k));l.addView(x,new LinearLayout.LayoutParams(0,72,1));}root.addView(l);}
        TextView in=btn("+ INGRESAR");in.setTextSize(22);in.setTextColor(Color.WHITE);in.setBackgroundColor(green);in.setGravity(Gravity.CENTER);in.setPadding(8,8,8,8);in.setOnClickListener(v->addIncome());root.addView(in,new LinearLayout.LayoutParams(-1,74));
        TextView out=btn("− REGISTRAR EGRESO");out.setTextColor(Color.WHITE);out.setBackgroundColor(red);out.setGravity(Gravity.CENTER);out.setPadding(8,8,8,8);out.setOnClickListener(v->expenseDialog());root.addView(out,new LinearLayout.LayoutParams(-1,66));

        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);String[] ns={"Historial","Informes","Turno","Mantenimiento","Config."};for(String n:ns){TextView x=btn(n);x.setTextSize(12);x.setOnClickListener(v->nav(n));nav.addView(x,new LinearLayout.LayoutParams(0,65,1));}root.addView(nav);
    }
    void key(String k){
        if(k.equals("C")) amount="";
        else if(amount.length()<8){
            if(amount.isEmpty() && k.equals("000")) return;
            amount+=k;
        }
        showAmount();
    }
    void backspace(){ if(!amount.isEmpty()) amount=amount.substring(0,amount.length()-1); showAmount(); }
    int val(){try{return Integer.parseInt(amount);}catch(Exception e){return 0;}}
    void showAmount(){amountView.setText("$ "+fmt(val()));}
    String fmt(int n){return NumberFormat.getIntegerInstance(new Locale("es","CL")).format(n);}
    void addIncome(){
        int n=val();
        if(n<=0){toast("Ingresa un monto");return;}
        if(driverId==0||vehicleId==0){toast("Primero selecciona conductor y móvil en Configuración");return;}
        long id=db.addMovement("IN",n,"Carrera","Ingreso",driverId,vehicleId);
        amount="";
        showAmount();
        refreshTotals();
        showUndoBar(id,n);
    }
    void showUndoBar(long id,int n){
        LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.HORIZONTAL);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(14,8,8,8);bar.setBackgroundColor(Color.rgb(229,231,235));
        TextView msg=tv("✓ $"+fmt(n)+" registrado",16,true);bar.addView(msg,new LinearLayout.LayoutParams(0,58,1));
        TextView undo=btn("DESHACER");undo.setTextSize(13);bar.addView(undo,new LinearLayout.LayoutParams(120,58));
        root.addView(bar,Math.min(4,root.getChildCount()));
        final boolean[] active={true};
        undo.setOnClickListener(v->{if(active[0]){active[0]=false;db.deleteMovement(id);refreshTotals();root.removeView(bar);toast("Ingreso eliminado");}});
        new Handler(Looper.getMainLooper()).postDelayed(()->{if(active[0]){active[0]=false;root.removeView(bar);}},4000);
    }
    void expenseDialog(){ LinearLayout l=form(); EditText monto=field("Monto",2);monto.setText(amount); EditText desc=field("Descripción (ej. combustible, peaje)",1); Spinner cat=new Spinner(this);String[] cats={"Combustible","Lavado","Peaje","Colación","Reparación","Mantenimiento","Otro"};cat.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,cats));l.addView(monto);l.addView(desc);l.addView(cat);new AlertDialog.Builder(this).setTitle("Registrar egreso").setView(l).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(d,w)->{int n=parse(monto);if(n>0){db.addMovement("OUT",n,desc.getText().toString(),cat.getSelectedItem().toString(),driverId,vehicleId);amount="";showAmount();refreshTotals();toast("Egreso registrado");}}).show(); }
    void refreshTotals(){Calendar c=Calendar.getInstance();c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);long from=c.getTimeInMillis(),to=from+86400000L;int[] t=db.totals(from,to,0,0);totalsView.setText("HOY  +$"+fmt(t[0])+"   −$"+fmt(t[1])+"\nGANANCIA  $"+fmt(t[0]-t[1]));}
    void updateIdentity(){for(String[] r:db.drivers())if(Long.parseLong(r[0])==driverId){driverName=r[1];break;}for(String[] r:db.vehicles())if(Long.parseLong(r[0])==vehicleId){vehicleName=r[1];break;}identityView.setText("Conductor: "+driverName+"  |  "+vehicleName);}
    void nav(String n){switch(n){case "Historial": history();break;case "Informes": reports();break;case "Turno":shift();break;case "Mantenimiento":maintenanceMenu();break;default:settings();}}
    void history(){List<String[]> rs=db.recent();StringBuilder s=new StringBuilder();for(String[] r:rs)s.append(r[1]).append("   ").append(r[2].equals("IN")?"+":"−").append("$").append(fmt(Integer.parseInt(r[3]))).append("\n").append(r[4]).append("\n\n");if(rs.isEmpty())s.append("Sin movimientos");new AlertDialog.Builder(this).setTitle("Historial").setMessage(s.toString()).setPositiveButton("Cerrar",null).show();}
    void reports(){String[] op={"Hoy","Esta semana","Este mes"};new AlertDialog.Builder(this).setTitle("Informe").setItems(op,(d,i)->report(i)).show();}
    void report(int mode){Calendar c=Calendar.getInstance();long to=System.currentTimeMillis()+1000; if(mode==0){c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);} else if(mode==1){c.set(Calendar.DAY_OF_WEEK,c.getFirstDayOfWeek());c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);} else {c.set(Calendar.DAY_OF_MONTH,1);c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);}int[] t=db.totals(c.getTimeInMillis(),to,0,0);String msg="Ingresos: $"+fmt(t[0])+"\nEgresos: $"+fmt(t[1])+"\n\nGanancia neta: $"+fmt(t[0]-t[1]);new AlertDialog.Builder(this).setTitle(mode==0?"Informe de hoy":mode==1?"Informe semanal":"Informe mensual").setMessage(msg).setPositiveButton("Cerrar",null).show();}
    void shift(){long id=db.currentShift();if(id==0){if(driverId==0||vehicleId==0){toast("Selecciona conductor y móvil");return;}EditText km=field("Kilometraje inicial",2);new AlertDialog.Builder(this).setTitle("Iniciar turno").setView(km).setNegativeButton("Cancelar",null).setPositiveButton("Iniciar",(d,w)->{db.openShift(driverId,vehicleId,parse(km));toast("Turno iniciado");}).show();}else{EditText km=field("Kilometraje final",2);new AlertDialog.Builder(this).setTitle("Cerrar turno").setView(km).setNegativeButton("Cancelar",null).setPositiveButton("Cerrar",(d,w)->{if(parse(km)<=0){toast("Debes ingresar kilometraje final");return;}db.closeShift(id,parse(km));toast("Turno cerrado");}).show();}}
    void maintenanceMenu(){if(vehicleId==0){toast("Selecciona un móvil");return;}String[] op={"Registrar mantenimiento","Ver historial del móvil"};new AlertDialog.Builder(this).setTitle("Mantenimiento · "+vehicleName).setItems(op,(d,i)->{if(i==0)addMaintenance();else showMaintenance();}).show();}
    void addMaintenance(){LinearLayout l=form();EditText km=field("Kilometraje",2),cost=field("Costo",2),type=field("Tipo (aceite, frenos, neumáticos...)",1),desc=field("Descripción",1),shop=field("Taller / mecánico",1),nextKm=field("Próximo km (opcional)",2),nextDate=field("Próxima fecha AAAA-MM-DD (opcional)",1);CheckBox cb=new CheckBox(this);cb.setText("Contabilizar también como egreso");cb.setChecked(true);for(View v:new View[]{km,cost,type,desc,shop,nextKm,nextDate,cb})l.addView(v);new AlertDialog.Builder(this).setTitle("Nuevo mantenimiento").setView(l).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(d,w)->{Integer nk=parse(nextKm)>0?parse(nextKm):null;db.addMaintenance(vehicleId,parse(km),parse(cost),type.getText().toString(),desc.getText().toString(),shop.getText().toString(),nk,nextDate.getText().toString(),cb.isChecked(),driverId);refreshTotals();toast("Mantenimiento guardado");}).show();}
    void showMaintenance(){StringBuilder s=new StringBuilder();for(String[] r:db.maintenance(vehicleId)){s.append(r[1]).append(" · ").append(r[4]).append(" · $").append(fmt(Integer.parseInt(r[3]))).append("\nKm: ").append(r[2]).append(" · ").append(r[5]).append("\nPróximo km: ").append(r[6]).append(" · fecha: ").append(r[7]).append("\n\n");}if(s.length()==0)s.append("Sin mantenimientos registrados");new AlertDialog.Builder(this).setTitle("Historial de mantenimiento").setMessage(s.toString()).setPositiveButton("Cerrar",null).show();}
    void settings(){String[] op={"Agregar conductor","Seleccionar conductor","Agregar móvil / patente","Seleccionar móvil","Cambiar montos rápidos"};new AlertDialog.Builder(this).setTitle("Configuración").setItems(op,(d,i)->{if(i==0)addDriver();else if(i==1)pickDriver();else if(i==2)addVehicle();else if(i==3)pickVehicle();else quickSettings();}).show();}
    void addDriver(){EditText e=field("Nombre del conductor",1);new AlertDialog.Builder(this).setTitle("Nuevo conductor").setView(e).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(d,w)->{if(!e.getText().toString().trim().isEmpty()){long id=db.addDriver(e.getText().toString());driverId=id;prefs.edit().putLong("driver",id).apply();updateIdentity();}}).show();}
    void pickDriver(){List<String[]> a=db.drivers();String[] n=new String[a.size()];for(int i=0;i<a.size();i++)n[i]=a.get(i)[1];new AlertDialog.Builder(this).setTitle("Conductor").setItems(n,(d,i)->{driverId=Long.parseLong(a.get(i)[0]);prefs.edit().putLong("driver",driverId).apply();updateIdentity();}).show();}
    void addVehicle(){LinearLayout l=form();EditText m=field("N.º de móvil",1),p=field("Patente",1),b=field("Marca / modelo (opcional)",1);l.addView(m);l.addView(p);l.addView(b);new AlertDialog.Builder(this).setTitle("Nuevo móvil").setView(l).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(d,w)->{if(!m.getText().toString().trim().isEmpty()&&!p.getText().toString().trim().isEmpty()){long id=db.addVehicle(m.getText().toString(),p.getText().toString(),b.getText().toString());vehicleId=id;prefs.edit().putLong("vehicle",id).apply();updateIdentity();}}).show();}
    void pickVehicle(){List<String[]> a=db.vehicles();String[] n=new String[a.size()];for(int i=0;i<a.size();i++)n[i]=a.get(i)[1];new AlertDialog.Builder(this).setTitle("Móvil").setItems(n,(d,i)->{vehicleId=Long.parseLong(a.get(i)[0]);prefs.edit().putLong("vehicle",vehicleId).apply();updateIdentity();}).show();}
    void quickSettings(){LinearLayout l=form();EditText a=field("Monto rápido 1",2),b=field("Monto rápido 2",2),c=field("Monto rápido 3",2);a.setText(String.valueOf(prefs.getInt("q1",2000)));b.setText(String.valueOf(prefs.getInt("q2",3000)));c.setText(String.valueOf(prefs.getInt("q3",5000)));l.addView(a);l.addView(b);l.addView(c);new AlertDialog.Builder(this).setTitle("Montos rápidos").setView(l).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(d,w)->{prefs.edit().putInt("q1",parse(a)).putInt("q2",parse(b)).putInt("q3",parse(c)).apply();toast("Guardado. Reinicia la app para ver los nuevos botones.");}).show();}
    LinearLayout form(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(30,10,30,0);return l;}
    EditText field(String hint,int type){EditText e=new EditText(this);e.setHint(hint);if(type==2)e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);return e;}
    int parse(EditText e){try{return Integer.parseInt(e.getText().toString().replace(".","").trim());}catch(Exception x){return 0;}}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
