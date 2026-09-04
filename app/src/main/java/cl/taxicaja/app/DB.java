package cl.taxicaja.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

public class DB extends SQLiteOpenHelper {
    public DB(Context c){ super(c,"taxicaja.db",null,1); }
    @Override public void onCreate(SQLiteDatabase d){
        d.execSQL("CREATE TABLE drivers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL)");
        d.execSQL("CREATE TABLE vehicles(id INTEGER PRIMARY KEY AUTOINCREMENT,mobile TEXT NOT NULL,plate TEXT NOT NULL,brand TEXT DEFAULT '')");
        d.execSQL("CREATE TABLE movements(id INTEGER PRIMARY KEY AUTOINCREMENT,ts INTEGER NOT NULL,type TEXT NOT NULL,amount INTEGER NOT NULL,description TEXT DEFAULT '',category TEXT DEFAULT '',driver_id INTEGER,vehicle_id INTEGER)");
        d.execSQL("CREATE TABLE shifts(id INTEGER PRIMARY KEY AUTOINCREMENT,start_ts INTEGER NOT NULL,end_ts INTEGER,start_km INTEGER,end_km INTEGER,driver_id INTEGER,vehicle_id INTEGER)");
        d.execSQL("CREATE TABLE maintenance(id INTEGER PRIMARY KEY AUTOINCREMENT,ts INTEGER NOT NULL,vehicle_id INTEGER NOT NULL,km INTEGER,cost INTEGER NOT NULL,type TEXT,description TEXT,workshop TEXT,next_km INTEGER,next_date TEXT,count_expense INTEGER DEFAULT 1)");
    }
    @Override public void onUpgrade(SQLiteDatabase d,int oldV,int newV){}

    public long addDriver(String name){ ContentValues v=new ContentValues(); v.put("name",name.trim()); return getWritableDatabase().insert("drivers",null,v); }
    public long addVehicle(String mobile,String plate,String brand){ ContentValues v=new ContentValues(); v.put("mobile",mobile.trim()); v.put("plate",plate.trim().toUpperCase()); v.put("brand",brand.trim()); return getWritableDatabase().insert("vehicles",null,v); }
    public List<String[]> drivers(){ return rows("SELECT id,name FROM drivers ORDER BY name"); }
    public List<String[]> vehicles(){ return rows("SELECT id,mobile||' · '||plate FROM vehicles ORDER BY mobile"); }
    private List<String[]> rows(String sql){ ArrayList<String[]> a=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery(sql,null); while(c.moveToNext()) a.add(new String[]{c.getString(0),c.getString(1)}); c.close(); return a; }

    public long addMovement(String type,int amount,String desc,String cat,long driver,long vehicle){ ContentValues v=new ContentValues(); v.put("ts",System.currentTimeMillis());v.put("type",type);v.put("amount",amount);v.put("description",desc);v.put("category",cat);v.put("driver_id",driver);v.put("vehicle_id",vehicle);return getWritableDatabase().insert("movements",null,v); }
    public int[] totals(long from,long to,long driver,long vehicle){ String where="ts>=? AND ts<?"; ArrayList<String> args=new ArrayList<>(Arrays.asList(String.valueOf(from),String.valueOf(to))); if(driver>0){where+=" AND driver_id=?";args.add(String.valueOf(driver));} if(vehicle>0){where+=" AND vehicle_id=?";args.add(String.valueOf(vehicle));} Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(CASE WHEN type='IN' THEN amount ELSE 0 END),0),COALESCE(SUM(CASE WHEN type='OUT' THEN amount ELSE 0 END),0) FROM movements WHERE "+where,args.toArray(new String[0])); c.moveToFirst(); int[] r={c.getInt(0),c.getInt(1)}; c.close(); return r; }
    public List<String[]> recent(){ ArrayList<String[]> a=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT id,datetime(ts/1000,'unixepoch','localtime'),type,amount,description FROM movements ORDER BY ts DESC LIMIT 100",null); while(c.moveToNext()) a.add(new String[]{c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4)}); c.close(); return a; }
    public void deleteMovement(long id){ getWritableDatabase().delete("movements","id=?",new String[]{String.valueOf(id)}); }

    public long openShift(long driver,long vehicle,int km){ ContentValues v=new ContentValues();v.put("start_ts",System.currentTimeMillis());v.put("start_km",km);v.put("driver_id",driver);v.put("vehicle_id",vehicle);return getWritableDatabase().insert("shifts",null,v); }
    public long currentShift(){ Cursor c=getReadableDatabase().rawQuery("SELECT id FROM shifts WHERE end_ts IS NULL ORDER BY id DESC LIMIT 1",null); long id=c.moveToFirst()?c.getLong(0):0;c.close();return id; }
    public void closeShift(long id,int km){ ContentValues v=new ContentValues();v.put("end_ts",System.currentTimeMillis());v.put("end_km",km);getWritableDatabase().update("shifts",v,"id=?",new String[]{String.valueOf(id)}); }

    public long addMaintenance(long vehicle,int km,int cost,String type,String desc,String workshop,Integer nextKm,String nextDate,boolean countExpense,long driver){ ContentValues v=new ContentValues();v.put("ts",System.currentTimeMillis());v.put("vehicle_id",vehicle);v.put("km",km);v.put("cost",cost);v.put("type",type);v.put("description",desc);v.put("workshop",workshop);if(nextKm!=null)v.put("next_km",nextKm);v.put("next_date",nextDate);v.put("count_expense",countExpense?1:0); long id=getWritableDatabase().insert("maintenance",null,v); if(countExpense && cost>0) addMovement("OUT",cost,type+": "+desc,"Mantenimiento",driver,vehicle); return id; }
    public List<String[]> maintenance(long vehicle){ ArrayList<String[]> a=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT id,date(ts/1000,'unixepoch','localtime'),km,cost,type,description,COALESCE(next_km,''),COALESCE(next_date,'') FROM maintenance WHERE vehicle_id=? ORDER BY ts DESC",new String[]{String.valueOf(vehicle)}); while(c.moveToNext()){ String[] r=new String[8];for(int i=0;i<8;i++)r[i]=c.getString(i);a.add(r);}c.close();return a; }
}
