package com.example.kzm.partybattlegame;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Random;

public class BattleActivity extends AppCompatActivity {
    String viewname[]=new String[4], ename[]=new String[4];
    double resistnum[][]=new double[8][8];
    boolean charaset[]=new boolean[4], death[]=new boolean[4];
    boolean enemyset[]=new boolean[4], enemydeath[]=new boolean[4];
    int cor,Emeny,level,power,turn,usemp,type,effectgo=0,cri,DFcri=1,hit,elenum,beginill=15,fullchange=17;
    int explus,getexp=0,timing,stage,m,n,k,max=0,code,txtcount,delaytime=2000,hitstandard = 90;
    double OF,DF,DM,load,aveDM=0,mul=2.0;
    int spdlist[]=new int[8],ctb[]=new int[8],count[]=new int[8];//0-3 味方 4-7 敵
    int irregular[][] = new int[8][fullchange];//0 resonance 1,2 atk 3,4 mtk 5,6 def 7,8 mef 9,10 spd 11,12 acc 13,14 eva 15 bind 16 poison
    int Emaxab[]={6,2,4,3,2,4,7,3,4,4}, Ekindmax = 10;
    int eability[][][] = new int[Ekindmax][8][5];//right 0 usemp 1 power 2 turn 3 type 4 effectgo
    String eabstr[][][]= new String[Ekindmax][8][3];//right 0 abilityname 1 target 2 element
    String half="#DAA520", quarter="#FF8C00", zero="#800000", thisturn="#8A2BE2";
    boolean myc=false,ec=false,miss=false,next;
    Random rnd=new Random();
    TextView tv,mytv[]=new TextView[4],etv[]=new TextView[4];
    Button bt,state;
    ImageView myiv[]=new ImageView[4];
    LinearLayout layout;
    Intent i;
    MyOpenHelper hp;
    SQLiteDatabase db;
    Cursor c,c2;
    Commons commons;
    ScrollView scrollView;
    //MediaPlayer music=new MediaPlayer();
    private final Handler handler = new Handler();
    private Runnable runnable;
    Commons.person_inbattle[] person_data = new Commons.person_inbattle[4];
    class enemydata{
        int enemy, HP, MP, HPMAX, MPMAX;
        double atk, mtk, def, mef, spd, acc, eva;
        boolean enemyset;
        String name, resist;
        public enemydata(){
            this.HPMAX = 0;
            this.MPMAX = 0;
            this.name = "";
            this.resist = "";
            this.enemyset = false;
        }
    }
    enemydata[] enemies = new enemydata[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);
        hp = new MyOpenHelper(this);
        db = hp.getWritableDatabase();
        commons = (Commons) getApplication();
        for(k=0;k<4;k++){
            person_data[k] = new Commons.person_inbattle();
            enemies[k] = new enemydata();
        }
        myiv[0]=findViewById(R.id.myview0);
        myiv[1]=findViewById(R.id.myview1);
        myiv[2]=findViewById(R.id.myview2);
        myiv[3]=findViewById(R.id.myview3);
        scrollView=findViewById(R.id.battlescroll);
        layout=findViewById(R.id.battletext);
        mytv[0] = findViewById(R.id.mystatus0);
        mytv[1] = findViewById(R.id.mystatus1);
        mytv[2] = findViewById(R.id.mystatus2);
        mytv[3] = findViewById(R.id.mystatus3);
        etv[0] = findViewById(R.id.enemystatus0);
        etv[1] = findViewById(R.id.enemystatus1);
        etv[2] = findViewById(R.id.enemystatus2);
        etv[3] = findViewById(R.id.enemystatus3);
        bt = (Button) findViewById(R.id.FandE);
        bt.setText("逃げる");
        state=findViewById(R.id.statement);
        state.setText("状態チェック");
        i = this.getIntent();
        level = i.getIntExtra("level",1);
        if(level>1) cor=(level-1)*2;
        else cor = 1;
        stage = i.getIntExtra("stage",0);
        if(stage==6) cor=10;
        if(stage==0 || stage==6) {
            /*audioSetup();
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            music.start();*/
        }
        battle_preparation();
        explus = i.getIntExtra("explus",0);
        Emeny = rnd.nextInt(4)+1;
        if(stage==4 || stage==6) Emeny=4;
        enemydata();
        levelminus(level);
        for(int i=1;i<10;i++)  dupname(i);
        for(k=0;k<4;k++) {
            if(person_data[k].getcharaset()) {
                ctb[k] = spdcheck((int) person_data[k].getdatadouble("spd"));
                spdlist[k] = (int)person_data[k].getdatadouble("spd");
                setiv(myiv[k],k);
                resistset(k);
                myiv[k].setOnClickListener(content);
                myiv[k].setEnabled(true);
                count[k]=1000;
                for(n=0;n<fullchange;n++) irregular[k][n]=0;
                max++;
            } else {
                ctb[k] = 0;
                spdlist[k] = 0;
                count[k] = 1000;
                myiv[k].setEnabled(false);
            }
            if(enemies[k].enemyset){
                enemies[k].HPMAX = enemies[k].HP;
                enemies[k].MPMAX = enemies[k].MP;
                ename[k] = enemies[k].name;
                enemyset[k] = enemies[k].enemyset;
                ctb[k+4] = spdcheck((int)enemies[k].spd);
                spdlist[k+4] = (int)enemies[k].spd;
                Eresistset(k);
                enemydeath[k] = false;
                count[k+4]=1000;
                for(n=0;n<fullchange;n++) irregular[k+4][n]=0;
                max++;
            } else {
                ctb[k+4] = 0;
                spdlist[k+4] = 0;
                count[k+4] = 1000;
                enemydeath[k] = true;
            }
        }
        setorder();
        myc=liveordeath("us");
        ec=liveordeath("enemy");
        statusset();
        txtcount=0;
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                myc=liveordeath("us");
                ec=liveordeath("enemy");
                if(!ec && myc) {
                    explus = explus + getexp;
                    if(stage==4){
                        i = new Intent(BattleActivity.this, MainActivity.class);
                        i.putExtra("level",level+1);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        i.putExtra("battle",true);
                    } else if(stage==6){
                        finish();
                    } else {
                        stage++;
                        i = new Intent(BattleActivity.this, Continue.class);
                        for(int s=0;s<4;s++) {
                            Commons.person_data_inbattle[s].remaining_HPwrite(person_data[s].HPMP_remaining_read("HP"));
                            Commons.person_data_inbattle[s].remaining_MPwrite(person_data[s].HPMP_remaining_read("MP"));
                            Commons.person_data_inbattle[s].setDeath(death[s]);
                        }
                        i.putExtra("level", level);
                        i.putExtra("stage", stage);
                    }
                } else {
                    i = new Intent(BattleActivity.this, MainActivity.class);
                    explus = 0;
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("battle",true);
                }
                i.putExtra("explus", explus);
                if(stage==6) finish();
                else startActivity(i);
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                turngo();
            }
        },delaytime);
        state.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                i=new Intent(BattleActivity.this, CheckActivity.class);
                i.putExtra("name",viewname);
                i.putExtra("set",charaset);
                i.putExtra("ename",ename);
                i.putExtra("eset",enemyset);
                i.putExtra("state0",irregular[0]);
                i.putExtra("state1",irregular[1]);
                i.putExtra("state2",irregular[2]);
                i.putExtra("state3",irregular[3]);
                i.putExtra("state4",irregular[4]);
                i.putExtra("state5",irregular[5]);
                i.putExtra("state6",irregular[6]);
                i.putExtra("state7",irregular[7]);
                startActivity(i);
            }
        });
    }

    /*@Override
    protected void onDestroy(){
        super.onDestroy();
        music.stop();
        // リセット
        music.reset();
        // リソースの解放
        music.release();
    }*/

    View.OnClickListener content = new View.OnClickListener() {
        public void onClick(View view) {
            int check=0;
            switch (view.getId()){
                case R.id.myview0:
                    check=0;
                    break;
                case R.id.myview1:
                    check=1;
                    break;
                case R.id.myview2:
                    check=2;
                    break;
                case R.id.myview3:
                    check=3;
                    break;
                default:
                    break;
            }
            if ((myc && ec)&& (code<4 && code>=0)) {
                i = new Intent(BattleActivity.this, AttackContents.class);
                i.putExtra("chara", person_data[code].getdataint("chara"));
                i.putExtra("name",code);
                i.putExtra("Lv", person_data[code].getdataint("Lv"));
                i.putExtra("MP", person_data[code].HPMP_remaining_read("MP"));
                i.putExtra("mylive",death);
                i.putExtra("our", viewname);
                i.putExtra("live",enemydeath);
                i.putExtra("enemy", ename);
                if(code==check)startActivityForResult(i, 1);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            autoscroll();
            String ab = data.getStringExtra("name");
            usemp=data.getIntExtra("usemp",0);
            power=data.getIntExtra("power",5);
            turn=data.getIntExtra("turn",0);
            type=data.getIntExtra("type",0);
            effectgo=data.getIntExtra("effectgo",0);
            load=loadset(type, power, turn);
            int target = data.getIntExtra("target",0);
            elenum = data.getIntExtra("element",-1);
            if(type>1 && type<5)myactionC(code, ab, target);
            else myactionAH(code, ab, target);
            mytv[code].setTextColor(Color.BLACK);
            HPcheck(code);
            ctbset(code,load);
            timing=0;
            code=-1;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    turngo();
                }
            },delaytime);
        }
    }

    public void turngo(){
        runnable = new Runnable() {
            @Override
            public void run() {
                myc=liveordeath("us");
                ec=liveordeath("enemy");
                if (myc && ec) {
                    ctb();
                    if (timing == 1 && code>3) {
                        timing = 0;
                        handler.postDelayed(this, delaytime);
                    }
                }
            }
        };
        handler.post(runnable);
    }

    public void ctb(){
        while(timing==0) {
            for (n = 0; n < 8; n++) {
                m = spdlist[n];
                count[m] = count[m] - (int)(ctb[m] * parametercheck(m,"spd"));
                if (count[m] <= 0){
                    code = m;
                    timing=1;
                    if(code>3){
                        if(bindcheck(code)){
                            tv=new TextView(this);
                            tv.setText("\n"+ename[code-4]+"は動けない");
                            layout.addView(tv);
                            tv.setGravity(Gravity.CENTER);
                            txtcount++;
                            ctbset(code,1.5);
                        } else {
                            enemyaction(enemies[code - 4].enemy, enemies[code - 4].MP);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {  turngo();                                }
                            },delaytime);
                        }
                    } else if(code<4){
                        tv=new TextView(this);
                        if(bindcheck(code)){
                            tv.setText("\n"+viewname[code]+"は動けない");
                            timing = 0;
                            ctbset(code,1.5);
                        } else {
                            tv.setText("\n"+viewname[code]+"のターン");
                            mytv[code].setTextColor(Color.parseColor(thisturn));
                        }
                        layout.addView(tv);
                        tv.setGravity(Gravity.CENTER);
                        txtcount++;
                    }
                    break;
                }//autoscroll();
            }
        }
    }

    public double parametercheck(int k, String str){
        double r;
        int val = 0;
        switch (str){
            case "atk":
                val = 2;
                break;
            case "mtk":
                val = 4;
                break;
            case "def":
                val = 6;
                break;
            case "mef":
                val = 8;
                break;
            case "spd":
                val = 10;
                break;
            case "acc":
                val = 12;
                break;
            case "eva":
                val = 14;
                break;
            default:
                break;
        }
        if(irregular[k][val-1]>0) r=1;
        else if(irregular[k][val]>0) r=-1;
        else r=0;
        return Math.pow(1.5, r);
    }

    public boolean bindcheck(int k){
        if(irregular[k][beginill]>0) return true;
        else return false;
    }

    public int poisoncheck(int k){
        if(irregular[k][beginill+1]>0) return person_data[k].HPMP_MAX_read("HP") / 32;
        else return 0;
    }

    public double loadset(int n,int p,int t){
        double num = 1.0;
        double standard = 25;
        if(n<2) num = p / standard * 1.0;
        else if(n>1 && n<5){
            if(t < 4) num = 1.0;
            else if(t >3) num = 2.0;
        } else if(n>4) num = 1.5;
        return num;
    }

    public void setchange(int me,int you){
        switch (type){
            case 2:
                if(code==me) turn++;
                irregular[me][effectgo*2-1] = turn;
                irregular[me][effectgo*2] = 0;
                break;
            case 3:
                irregular[you][effectgo*2-1] = 0;
                irregular[you][effectgo*2] = turn;
                break;
            case 4:
                irregular[you][beginill+effectgo-1]=turn;
                break;
            default:
                break;
        }
    }

    public void ctbset(int me,double load){
        if(count[me]<=0){
            count[me]=(int)(1000*load);
        }
        for(k=1;k<fullchange;k++){
            if(irregular[me][k]>0)irregular[me][k]--;
        }
    }

    public double acccor(String str){
        double mul=1.0;
        switch (str){
            case "SD":
                if(effectgo<2) mul=1.1;
                else if(effectgo>=2) mul=0.9;
                break;
            case "LD":
                if(effectgo>=2) mul=1.1;
                else if(effectgo<2) mul=0.9;
                break;
            default:
                break;
        }
        return mul;
    }

    public void myactionAH(int me, String ab, int appoint){
        if (usemp > person_data[me].HPMP_remaining_read("MP")) {
            ab = "ただの攻撃";
            usemp = 0;
            type = 0;
            power = 5;
            effectgo = 1;
        }
        if(effectgo<3) OF = person_data[me].getdatadouble("atk") * parametercheck(me, "atk");
        else if(effectgo==3 || type==5) OF = person_data[me].getdatadouble("mtk") * parametercheck(me, "mtk");
        person_data[me].remaining_MPwrite(person_data[me].HPMP_remaining_read("MP") - usemp);
        if (person_data[me].HPMP_remaining_read("MP") < 0) person_data[me].remaining_MPwrite(0);
        if(type<2) {
            if(appoint==6){
                aveDM=0;
                OF *= 0.95;
                for(k=0;k<4;k++){
                    if(enemydeath[k]==false)mydamagedecition(me,k);
                }
            } else mydamagedecition(me,appoint);
        } else if(type==5){
            if(appoint==6){
                aveDM=0;
                OF *= 0.95;
                for(k=0;k<4;k++){
                    if(death[k]==false)myhealing(me,k);
                }
            } else myhealing(me,appoint);
        }
        String name = new String();
        if(appoint==6) name="";
        else if(type<2) name = ename[appoint];
        else if(type==5) name = viewname[appoint];
        actset(viewname[me], ab, appoint, name,"敵");
    }

    public void EactionAH(int me, String ab, int appoint){
        me = me-4;
        if(effectgo<3) OF = enemies[me].atk * parametercheck(me+4, "atk");
        else if(effectgo==3 || type==5) OF = enemies[me].mtk * parametercheck(me+4, "mtk");
        enemies[me].MP = enemies[me].MP - usemp;
        if (enemies[me].MP < 0) enemies[me].MP = 0;
        if(type<2) {
            if(appoint==6){
                aveDM=0;
                OF *= 0.95;
                for(k=0;k<4;k++){
                    if(death[k]==false)Edamagedecition(me,k);
                }
            } else Edamagedecition(me,appoint);
        } else if(type==5){
            if(appoint==6){
                aveDM=0;
                OF *= 0.95;
                for(k=0;k<4;k++){
                    if(enemydeath[k]==false)Ehealing(me,k);
                }
            } else Ehealing(me,appoint);
        }
        String name = new String();
        if(appoint==6) name="";
        else if(type<2) name = viewname[appoint];
        else if(type==5) name = ename[appoint];
        actset(ename[me], ab, appoint, name,"味方");
    }

    public void myactionC(int me, String ab, int appoint){
        person_data[me].remaining_MPwrite(person_data[me].HPMP_remaining_read("MP") - usemp);
        if (person_data[me].HPMP_remaining_read("MP") < 0) person_data[me].remaining_MPwrite(0);
        if(appoint==6){
            for(k=0;k<4;k++) setchange(k,k+4);
        } else {
            setchange(appoint,appoint+4);
        }
        String name = new String();
        if(appoint==6) name="";
        else if(type==3 || type==4) name = ename[appoint];
        else if(type==2) name = viewname[appoint];
        actset(viewname[me], ab, appoint, name,"敵");
    }

    public void EactionC(int me, String ab, int appoint){
        me=me-4;
        enemies[me].MP = enemies[me].MP - usemp;
        if (enemies[me].MP < 0) enemies[me].MP = 0;
        if(appoint==6){
            for(k=0;k<4;k++) setchange(k+4,k);
        } else {
            setchange(appoint+4,appoint);
        }
        String name = new String();
        if(appoint==6) name="";
        else if(type==3 || type==4) name = viewname[appoint];
        else if(type==2) name = ename[appoint];
        actset(ename[me], ab, appoint, name,"味方");
    }

    public void mydamagedecition(int me, int appoint){
        int R = rnd.nextInt(5);
        cri = rnd.nextInt(100)+1;
        if(effectgo<3) DF = enemies[appoint].def * parametercheck(appoint, "def");
        else if(effectgo==3) DF = enemies[appoint].mef * parametercheck(appoint, "mef");
        DFcri = 1;
        if(cri>97) DFcri=2;
        DM = Math.pow(OF,mul) * (double) power * damagemul(appoint+4, elenum) / (DF/DFcri)
                /(((person_data[me].getdataint("Lv") + 9)/10)*10) + (double) person_data[me].getdataint("Lv") / 4 - (double) R;
        if (DM < 1) DM = 1;
        if(type<2){
            R = rnd.nextInt(100);
            hit=(int)((person_data[me].getdatadouble("acc") * parametercheck(me,"acc")
                    - enemies[appoint].eva * parametercheck(appoint+4, "eva")) / 50
                    + hitstandard * acccor(person_data[me].getdataString("specialty")));
            if(R<hit) {
                enemies[appoint].HP = enemies[appoint].HP - (int)DM;
                if (enemies[appoint].HP < 0) enemies[appoint].HP = 0;
                miss=false;
            } else {
                miss=true;
                DM=0;
            }
        }
        aveDM += DM;
    }

    public void Edamagedecition(int me, int appoint){
        int R = rnd.nextInt(5);
        cri = rnd.nextInt(100)+1;
        if(effectgo<3) DF = person_data[appoint].getdatadouble("def") * parametercheck(appoint, "def");
        else if(effectgo==3) DF = person_data[appoint].getdatadouble("mef") * parametercheck(appoint, "mef");
        DFcri = 1;
        if(cri>97) DFcri=2;
        DM = Math.pow(OF,mul) * (double) power * damagemul(appoint, elenum) / (DF/DFcri) * cor / (cor*10)  - (double) R;
        if (DM < 1) DM = 1;
        if(type<2){
            R = rnd.nextInt(100);
            hit=(int)((enemies[me].acc * parametercheck(me,"acc") - person_data[appoint].getdatadouble("eva")
                    * parametercheck(appoint, "eva")) / 50 + hitstandard);
            if(R<hit) {
                int tmp = person_data[appoint].HPMP_remaining_read("HP") - (int)DM;
                person_data[appoint].remaining_HPwrite(tmp);
                if (person_data[appoint].HPMP_remaining_read("HP") < 0) person_data[appoint].remaining_HPwrite(0);
                miss=false;
            } else {
                miss=true;
                DM=0;
            }
        }
        aveDM+=DM;
    }

    public void myhealing(int me, int appoint){
        double R = (rnd.nextInt(5)+6)/100+0.7;
        DM = Math.pow(OF,1.5) * R * (double) power /
                (((person_data[me].getdataint("Lv") + 9)/10)*10)+ (double) person_data[me].getdataint("Lv") / 4;
        int sub,tmp;
        if(effectgo==1){
            sub = person_data[appoint].HPMP_MAX_read("HP") - person_data[appoint].HPMP_remaining_read("HP");
            tmp = person_data[appoint].HPMP_remaining_read("HP") + (int)DM;
            person_data[appoint].remaining_HPwrite(tmp);
            if(person_data[appoint].HPMP_remaining_read("HP") > person_data[appoint].HPMP_MAX_read("HP")){
                person_data[appoint].remaining_HPwrite(person_data[appoint].HPMP_MAX_read("HP"));
                DM = sub;
            }
        } else if(effectgo==2){
            sub = person_data[appoint].HPMP_MAX_read("MP") - person_data[appoint].HPMP_remaining_read("MP");
            tmp = person_data[appoint].HPMP_remaining_read("MP") + (int)DM;
            person_data[appoint].remaining_MPwrite(tmp);
            if(person_data[appoint].HPMP_remaining_read("MP") > person_data[appoint].HPMP_MAX_read("MP")){
                person_data[appoint].remaining_MPwrite(person_data[appoint].HPMP_MAX_read("MP"));
                DM = sub;
            }
        } else if(effectgo==3){
            for(k=beginill;k<fullchange;k++) irregular[appoint][k]=0;
        } else if(effectgo==4){
            if(death[appoint]){
                death[appoint] = false;
                person_data[appoint].remaining_HPwrite(person_data[appoint].HPMP_MAX_read("HP") / 2);
                ctb[appoint] = spdcheck((int) person_data[appoint].getdatadouble("spd"));
                count[appoint] = 2000;
                myiv[k].setEnabled(true);
            }
        }
    }

    public void Ehealing(int me, int appoint){
        double R = (rnd.nextInt(5)+6)/100+0.7;
        DM = Math.pow(OF,1.5) * R * (double) power /(((cor+9)/10)*10)+ (double)cor / 4;
        int sub;
        if(effectgo==1){
            sub = enemies[appoint].HPMAX - enemies[appoint].HP;
            enemies[appoint].HP = enemies[appoint].HP + (int)DM;
            if(enemies[appoint].HP > enemies[appoint].HPMAX){
                enemies[appoint].HP = enemies[appoint].HPMAX;
                DM=sub;
            }
        } else if(effectgo==2){
            sub = enemies[appoint].MPMAX - enemies[appoint].MP;
            enemies[appoint].MP = enemies[appoint].MP + (int)DM;
            if(enemies[appoint].MP > enemies[appoint].MPMAX){
                enemies[appoint].MP = enemies[appoint].MPMAX;
                DM=sub;
            }
        } else if(effectgo==3){
            for(k=beginill;k<fullchange;k++) irregular[appoint+4][k]=0;
        } else if(effectgo==4){
            if(enemydeath[appoint]==true){
                enemydeath[appoint]=false;
                enemies[appoint].HP = enemies[appoint].HPMAX / 2;
                ctb[appoint+4] = spdcheck((int) enemies[appoint].spd);
                count[appoint+4] = 2000;
            }
        }
    }

    public void actset(String me, String ab, int appoint, String you, String party){
        //autoscroll();
        String change="";
        switch (effectgo){
            case 1:
                if(type/2==1)change="攻撃";
                else if(type==4) change="拘束";
                else if(type==5) change="HP";
                break;
            case 2:
                if(type/2==1)change="魔攻";
                else if(type==4) change="毒";
                else if(type==5) change="MP";
                break;
            case 3:
                change="防御";
                break;
            case 4:
                change="魔防";
                break;
            case 5:
                change="速さ";
                break;
            case 6:
                change="命中";
                break;
            case 7:
                change="回避";
                break;
            default:
                change="";
                break;
        }
        String critical;
        if(cri>97) critical="クリティカル！";
        else critical="";
        tv=new TextView(this);
        if (appoint==6){
            if(type==1){
                tv.setText(me+"の行動:"+ab+"\n"+party+"パーティーに平均" + (int) aveDM/4 + "のダメージ");
            }
            else if(type==2) tv.setText(me+"の行動:"+ab+"\n"+party+"全員の"+change+"が上がった");
            else if(type==3) tv.setText(me+"の行動:"+ab+"\n"+party+"パーティーの"+change+"が下がった");
            else if(type==4) tv.setText(me+"の行動:"+ab+"\n"+party+"パーティーは"+change+"状態になった");
            else if(type==5) tv.setText(me+"の行動:"+ab+"\n"+party+"全員の"+change+"が回復した");
        } else {
            if(type==1){
                if(miss) tv.setText(me+"の行動:"+ab+"\nミス！ "+me+"の攻撃は外れた");
                else tv.setText(me+"の行動:"+ab+"\n"+critical+you+"に" + (int) DM + "のダメージ");
            }
            else if(type==2) tv.setText(me+"の行動:"+ab+"\n"+you+"の"+change+"が上がった");
            else if(type==3) tv.setText(me+"の行動:"+ab+"\n"+you+"の"+change+"が下がった");
            else if(type==4) tv.setText(me+"の行動:"+ab+"\n"+you+"は"+change+"状態になった");
            else if(type==5){
                if(effectgo<3) tv.setText(me+"の行動:"+ab+"\n"+you+"の"+change+"が"+(int)DM+"回復した");
                else if(effectgo==3) tv.setText(me+"の行動:"+ab+"\n"+you+"の状態異常が癒えた");
                else if(effectgo==4) tv.setText(me+"の行動:"+ab+"\n"+you+"が戦線復帰した");
            }
        }
        layout.addView(tv);
        tv.setGravity(Gravity.CENTER);
        txtcount=txtcount+2;
        statusset();
        for(int b=0;b<4;b++) {
            if (person_data[b].HPMP_remaining_read("HP") <= 0 && !death[b]) {
                tv = new TextView(BattleActivity.this);
                tv.setText(viewname[b] + "は戦闘不能になってしまった……");
                layout.addView(tv);
                tv.setGravity(Gravity.CENTER);
                txtcount++;
            }
            if (enemies[b].HP <= 0 && !enemydeath[b]) {
                tv = new TextView(BattleActivity.this);
                tv.setText(ename[b] + "を倒した！");
                layout.addView(tv);
                tv.setGravity(Gravity.CENTER);
                txtcount++;
                etv[b].setText("");
            }
        }
        if(!liveordeath("enemy")) bt.setText("勝利！");
        //autoscroll();
    }

    public void enemyaction(int me,/*enemy[code-4]*/int mp /*eMP[code-4]*/){
        autoscroll();
        int target, num;
        while(true){
            target=rnd.nextInt(4);
            if(!death[target]) break;
        }
        while(true){
            num = rnd.nextInt(Emaxab[me]);
            usemp = eability[me][num][0];
            if(mp >= usemp) break;
        }
        String ab = eabstr[me][num][0];
        power = eability[me][num][1];
        turn = eability[me][num][2];
        type = eability[me][num][3];
        effectgo = eability[me][num][4];
        load = loadset(type, power, turn);
        String targetstr = eabstr[me][num][1];
        elenum = elementset(eabstr[me][num][2]);
        if(targetstr.equals("all") || targetstr.equals("allus")) target=6;
        if(type>1 && type<5) EactionC(code, ab, target);
        else EactionAH(code, ab, target);
        ctbset(code,load);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                turngo();
            }
        },delaytime);
    }

    public void statusset(){
        for(k=0;k<4;k++) {
            if (person_data[k].getcharaset()) {
                mytv[k].setText(viewname[k] + "\nLv" +
                        person_data[k].getdataint("Lv") +
                        "\nHP " + (person_data[k].HPMP_remaining_read("HP") - poisoncheck(k)) +
                        "\nMP " + person_data[k].HPMP_remaining_read("MP"));
                HPcheck(k);
            } else mytv[k].setText("");
            if (enemies[k].enemyset && !enemydeath[k]) {
                etv[k].setText(ename[k]+"\nHP " + (enemies[k].HP - poisoncheck(k)) + "\nMP " + enemies[k].MP);
            } else etv[k].setText("");
        }
    }

    public void HPcheck(int s){
        int HP = person_data[s].HPMP_remaining_read("HP");
        int HPMAX = person_data[s].HPMP_MAX_read("HP");
        if(HP < HPMAX/2 && HP >= HPMAX/4) mytv[s].setTextColor(Color.parseColor(half));
        else if(HP < HPMAX/4 && HP > 0) mytv[s].setTextColor(Color.parseColor(quarter));
        else if(HP <= 0) mytv[s].setTextColor(Color.parseColor(zero));
        else mytv[s].setTextColor(Color.BLACK);
    }

    public int spdcheck(int sc){
        return sc/50+1;
    }

    public boolean liveordeath(String str){
        boolean check=true;
        int DH=0,a;
        if(str.equals("us")){
            for(a=0;a<4;a++) {
                if (person_data[a].HPMP_remaining_read("HP")<=0){
                    ctb[a]=0;
                    for(k=0;k<fullchange;k++) irregular[a][k]=0;
                    death[a]=true;
                    myiv[a].setEnabled(false);
                    DH++;
                }
            }
        } else if(str.equals("enemy")){
            for(a=0;a<4;a++) {
                if (enemies[a].HP<=0){
                    ctb[a+4]=0;
                    for(k=0;k<fullchange;k++) irregular[a+4][k]=0;
                    enemydeath[a]=true;
                    DH++;
                }
            }
        }
        if(DH>=4){
            check=false;
        }
        return check;
    }

    public void enemydata(){
        /*for(int v=0;v<Ekindmax;v++){
            for(int w=0;w<8;w++) {
                eabstr[v][w][1]="";
                eabstr[v][w][2]="";
            }
        }*/
        for(k=0;k<Emeny;k++) {
            int d = rnd.nextInt(2 + level)+1;
            if(d>10) d = (d - 10);
            if(level==100) d = rnd.nextInt(3)+7;
            Cursor c = db.query("enemy", new String[]{"code", "name", "HP", "MP", "ATK", "MTK", "DEF", "MEF", "SPD", "ACC", "EVA", "resist", "explus"}, null, null, null, null, null);
            next = c.moveToFirst();
            while (next) {
                enemies[k].enemy = c.getInt(0);
                if((stage==4 || stage==6) && k==1) d=0;
                if (d == enemies[k].enemy) {
                    enemies[k].name = c.getString(1);
                    enemies[k].HP = c.getInt(2) * cor;
                    enemies[k].MP = c.getInt(3) * cor;
                    enemies[k].atk = c.getInt(4) * cor;
                    enemies[k].mtk = c.getInt(5) * cor;
                    enemies[k].def = c.getInt(6) * cor;
                    enemies[k].mef = c.getInt(7) * cor;
                    enemies[k].spd = c.getInt(8) * cor;
                    enemies[k].acc = c.getInt(9) * cor;
                    enemies[k].eva = c.getInt(10) * cor;
                    enemies[k].resist = c.getString(11);
                    getexp += c.getInt(12) * cor;
                    enemies[k].enemyset = true;
                    break;
                }
                next = c.moveToNext();
            }
            boolean flag=false;
            for(int i=0;i<k;i++){
                if(enemies[i].enemy == enemies[k].enemy) flag=true;
            }
            if(flag) continue;
            String sel[]=new String[1];
            sel[0]=Integer.toString(enemies[k].enemy);
            int j=1;
            int check,checklv;
            int abnum[]=new int[Emaxab[enemies[k].enemy]];
            c2 = db.query("Elearning", new String[] { "code", "abilityID", "level"}, "code = ?",
                    sel, null, null, null);
            next = c2.moveToFirst();
            while (next) {
                check = c2.getInt(0);
                if (check == enemies[k].enemy) {
                    checklv = c2.getInt(2);
                    if(checklv <= level){
                        abnum[j]= c2.getInt(1);
                    }
                    j++;
                }
                next = c2.moveToNext();
            }
            for(int i=1;i<j;i++){
                String sel2[]=new String[1];
                sel2[0]=Integer.toString(abnum[i]);
                Cursor c3 = db.query("ability", new String[] { "abilityID", "name", "usemp", "power","turn", "type", "target", "element"}, "abilityID = ?",
                        sel2, null, null, null);
                next = c3.moveToFirst();
                while (next) {
                        eabstr[enemies[k].enemy][i][0] = c3.getString(1);
                        eability[enemies[k].enemy][i][0] = c3.getInt(2);
                        eability[enemies[k].enemy][i][1]  = c3.getInt(3);
                        eability[enemies[k].enemy][i][2]  = c3.getInt(4);
                        String typecode = c3.getString(5);
                        eabstr[enemies[k].enemy][i][1] = c3.getString(6);
                        eabstr[enemies[k].enemy][i][2] = c3.getString(7);
                        checkmethod(i, typecode);

                    next = c3.moveToNext();
                }
                c3.close();
            }
            eabstr[enemies[k].enemy][0][0] = "通常攻撃";
            eability[enemies[k].enemy][0][0] = 0;
            eability[enemies[k].enemy][0][1]  = 25;
            eability[enemies[k].enemy][0][2]  = 0;
            String typecode = "direct";
            eabstr[enemies[k].enemy][0][1] = "single";
            eabstr[enemies[k].enemy][0][2] = "normal";
            checkmethod(0, typecode);
        }
    }


    public void checkmethod(int s,String str){
        switch (str) {
            case "direct":
               eability[enemies[k].enemy][s][3] = 1;
               eability[enemies[k].enemy][s][4] = 1;
                break;
            case "indirect":
               eability[enemies[k].enemy][s][3] = 1;
               eability[enemies[k].enemy][s][4] = 2;
                break;
            case "magic":
               eability[enemies[k].enemy][s][3] = 1;
               eability[enemies[k].enemy][s][4] = 3;
                break;
            case "atksupport":
               eability[enemies[k].enemy][s][3] = 2;
               eability[enemies[k].enemy][s][4] = 1;
                break;
            case "mtksupport":
               eability[enemies[k].enemy][s][3] = 2;
               eability[enemies[k].enemy][s][4] = 2;
                break;
            case "defsupport":
               eability[enemies[k].enemy][s][3] = 2;
               eability[enemies[k].enemy][s][4] = 3;
                break;
            case "mefsupport":
               eability[enemies[k].enemy][s][3] = 2;
               eability[enemies[k].enemy][s][4] = 4;
                break;
            case "spdsupport":
               eability[enemies[k].enemy][s][3] = 2;
               eability[enemies[k].enemy][s][4] = 5;
                break;
            case "accsupport":
               eability[enemies[k].enemy][s][3] = 2;
               eability[enemies[k].enemy][s][4] = 6;
                break;
            case "evasupport":
               eability[enemies[k].enemy][s][3] = 2;
               eability[enemies[k].enemy][s][4] = 7;
                break;
            case "atkdown":
               eability[enemies[k].enemy][s][3] = 3;
               eability[enemies[k].enemy][s][4] = 1;
                break;
            case "mtkdown":
               eability[enemies[k].enemy][s][3] = 3;
               eability[enemies[k].enemy][s][4] = 2;
                break;
            case "defdown":
               eability[enemies[k].enemy][s][3] = 3;
               eability[enemies[k].enemy][s][4] = 3;
                break;
            case "mefdown":
               eability[enemies[k].enemy][s][3] = 3;
               eability[enemies[k].enemy][s][4] = 4;
                break;
            case "spddown":
               eability[enemies[k].enemy][s][3] = 3;
               eability[enemies[k].enemy][s][4] = 5;
                break;
            case "accdown":
               eability[enemies[k].enemy][s][3] = 3;
               eability[enemies[k].enemy][s][4] = 6;
                break;
            case "evadown":
               eability[enemies[k].enemy][s][3] = 3;
               eability[enemies[k].enemy][s][4] = 7;
                break;
            case "bind":
               eability[enemies[k].enemy][s][3] = 4;
               eability[enemies[k].enemy][s][4] = 1;
                break;
            case "poison":
               eability[enemies[k].enemy][s][3] = 4;
               eability[enemies[k].enemy][s][4] = 2;
                break;
            case "hpheal":
               eability[enemies[k].enemy][s][3] = 5;
               eability[enemies[k].enemy][s][4] = 1;
                break;
            case "mpheal":
               eability[enemies[k].enemy][s][3] = 5;
               eability[enemies[k].enemy][s][4] = 2;
                break;
            case "illheal":
               eability[enemies[k].enemy][s][3] = 5;
               eability[enemies[k].enemy][s][4] = 3;
                break;
            default:
                break;
        }
    }

    public int elementset(String str){
        switch (str){
            case "fire":
                return 0;
            case "ice":
                return 1;
            case "thunder":
                return 2;
            case "water":
                return 3;
            case "wind":
                return 4;
            case "ground":
                return 5;
            case "light":
                return 6;
            case "dark":
                return 7;
            default:
                return -1;
        }
    }

    /*private boolean audioSetup(){
        boolean fileCheck = false;
        // rawにファイルがある場合
        music = MediaPlayer.create(this,R.raw.overdose);
        // 音量調整を端末のボタンに任せる
        music.setLooping(true);
        music.seekTo(0);
        fileCheck = true;
        return fileCheck;
    }*/

    public double damagemul(int appoint,int num){
        if(num==-1) return 1;
        else return 1 + 0.5 * resistnum[appoint][num];
    }

    public void resistset(int a){
        String str[] = person_data[a].getdataString("resist").split(",");
        for(int i=0;i<8;i++){
            resistnum[a][i] = Double.parseDouble(str[i]);
        }
    }

    public void Eresistset(int a){
        //String str[] = enemies[a].resist.split(",");
        String str[] = enemies[a].resist.split(",");
        for(int i=0;i<8;i++){
            resistnum[a+4][i] = Double.parseDouble(str[i]);
        }
    }
    
    public void battle_preparation(){
        for(int s=0;s<4;s++){
            person_data[s].setcharaset(commons.person_data[s].getcharaset());
            person_data[s].setdataint("chara", commons.person_data[s].getdataint("chara"));
            person_data[s].setdataString("viewname", commons.person_data[s].getdataString("viewname"));
            person_data[s].setdataint("Lv", commons.person_data[s].getdataint("Lv"));
            person_data[s].setdatadouble("atk", commons.person_data[s].getdatadouble("atk"));
            person_data[s].setdatadouble("mtk", commons.person_data[s].getdatadouble("mtk"));
            person_data[s].setdatadouble("def", commons.person_data[s].getdatadouble("def"));
            person_data[s].setdatadouble("mef", commons.person_data[s].getdatadouble("mef"));
            person_data[s].setdatadouble("spd", commons.person_data[s].getdatadouble("spd"));
            person_data[s].setdatadouble("acc", commons.person_data[s].getdatadouble("acc"));
            person_data[s].setdatadouble("eva", commons.person_data[s].getdatadouble("eva"));
            person_data[s].setdataString("specialty", commons.person_data[s].getdataString("specialty"));
            person_data[s].setdataString("resist", commons.person_data[s].getdataString("resist"));
            person_data[s].remaining_HPwrite(commons.person_data_inbattle[s].HPMP_remaining_read("HP"));
            person_data[s].remaining_MPwrite(commons.person_data_inbattle[s].HPMP_remaining_read("MP"));
            person_data[s].MAX_HPwrite(commons.person_data_inbattle[s].HPMP_MAX_read("HP"));
            person_data[s].MAX_MPwrite(commons.person_data_inbattle[s].HPMP_MAX_read("MP"));
            charaset[s] = person_data[s].getcharaset();
            viewname[s] = person_data[s].getdataString("viewname");
            death[s] = commons.person_data_inbattle[s].getDeath();
        }
    }

    public void dupname(int a){
        int i,j=0;
        int du[]=new int[4];
        for(i=0;i<Emeny;i++){
            if(enemies[i].enemy==a){
                du[j]=i;
                j++;
            }
        }
        if(j>1){
            for(i=0;i<j;i++){
                if(i==0)enemies[du[i]].name = enemies[du[i]].name.concat("A");
                if(i==1)enemies[du[i]].name = enemies[du[i]].name.concat("B");
                if(i==2)enemies[du[i]].name = enemies[du[i]].name.concat("C");
                if(i==3)enemies[du[i]].name = enemies[du[i]].name.concat("D");
            }
        }

    }

    public void levelminus(int num){
        if(num<2){
            Emaxab[0]=2;
            Emaxab[2]=2;
            Emaxab[3]=2;
        } else if(num<3){
            Emaxab[0]=3;
            Emaxab[2]=2;
            Emaxab[3]=2;
        } else if(num<4){
            Emaxab[0]=5;
            Emaxab[2]=3;
        } else if(num<5){
            Emaxab[2]=3;
        }
    }

    public void setorder(){
        int num[]=new int[8];
        int i,j=0,maxi;
        for(i=0;i<max;i++){
            num[i]=0;
        }
        while(j<8){
            i=0;
            maxi=-1;
            while(i<8){
                if(maxi<spdlist[i]){
                    maxi=spdlist[i];
                    num[j]=i;
                }
                i++;
            }
            spdlist[num[j]]=-1;
            j++;
        }
        for(i=0;i<8;i++){
            spdlist[i]=num[i];
        }
    }

    public void autoscroll(){
        layout.removeAllViews();
        /*if(txtcount>10){
            scrollView.post(new Runnable() {
                public void run() {
                    scrollView.scrollTo(0, scrollView.getBottom());
                }
            });
            txtcount=0;
        }*/
    }

    public void setiv(ImageView iv,int v){
        int num;
        c = db.query("personimage",new String[]{"code","charaimage"},null,null,null,null,null);
        next = c.moveToFirst();
        while (next) {
            num = c.getInt(0);
            if (num == person_data[v].getdataint("chara")) {
                iv.setImageBitmap(getImage(c.getBlob(1)));
                break;
            }
            next = c.moveToNext();
        }

    }

    protected void onPause(){
        super.onPause();
        c.close();
    }

    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    public void onBackPressed() {
    }
}
