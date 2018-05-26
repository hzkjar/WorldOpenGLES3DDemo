package com.wordopengl.zhengzha;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import com.wordopengl.zhengzha.migong.Maze;
import com.worldopengles.zhengzha.Game.DrawRenderer;
import com.worldopengles.zhengzha.Game.GameRun;
import com.worldopengles.zhengzha.Game.Geometry;
import com.worldopengles.zhengzha.Game.Texture;
import com.worldopengles.zhengzha.Game.WLoader;
import com.worldopengles.zhengzha.Game.WorldMatrix;
import com.worldopengles.zhengzha.Game.WorldTool;
import com.worldopengles.zhengzha.Geometry.TextureGeometry;
import com.worldopengles.zhengzha.Wtool.ColorTool;
import com.worldopengles.zhengzha.Wtool.MeshType;
import com.worldopengles.zhengzha.Wtool.WMesh;
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import android.util.Log;
import android.widget.Toast;


public class MainActivity extends Activity
{
	
	//渲染画面的控件
	private GameRun gamerun;
	//渲染器
	private DrawRenderer renderer;
	//世界配置
	private WorldTool word;
	//按钮
	private Button btS,btX,btZ,btY;
	//移动速度
	private float 速度=0.05f;
	//地图数据
	//Z X
	private int[][] map;
	//墙
	public static final int QIANG=1;
	//空气
	public static final int KONGQI=0;
	//顶点数据集合
	private ArrayList<Float> vertexList;
	//纹理坐标集合
	private ArrayList<Float> texcoorList;
	//法线集合
	private ArrayList<Float> normalList;
	//每个区块的宽度/2
	private float blockWidth=1f;
	//地图几何体
	private TextureGeometry mapGeometry;
	//地板几何体
	private TextureGeometry dibanGeometry;
	//地图顶点数组
	private float[] mapVertex;
	//地图纹理坐标数组
	private float[] mapTexcoor;
	//地图法线
	private float[] mapNormal;
	//球的坐标
	private float x,z;
	//球几何体
	private TextureGeometry ball;
	//球半径
	private float 半径=0.3f;
	
	//摄像头参数
	//摄像头位置和观察点
	private float cx,cy,cz,lx,ly,lz;
	//角度
	private float Cangle=0;
	//走路开关
	private boolean shang=false,xia=false,zuo=false,you=false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
	
		setContentView(R.layout.main);
		//生成二维数组地图数据
		map=initMapData(5,5);
		//生成顶点集合
		initVertex();
		//初始化控件
		initView();
		
		//新建一个世界工具对象
		word=new WorldTool(this);
		//设置雾距离
		word.setFogStratAndEnd(10,20);
		//设置雾颜色，黑色
		word.setFogColor(new ColorTool(0,0,0,1));
		//是否画阴影
	   word.setDrawShadow(true);
	   //定向光向量方向
	   word.setLightDirection(0.3f,-1,-1);
	   //是否有定向光光照
	   word.setDrawLightDirection(true);
	   //设置定向光的颜色
	   word.setLightDirectionColor(new ColorTool(1,1,1,1));
	   		//初始化渲染器
		init();
		
		//为渲染器设置世界配置
		gamerun.setWordTool(word);
		//为渲染控件设置渲染器
		gamerun.setDrawRenderer(renderer);
		
		//开启逻辑
		login.setName("login");
		login.start();
	}
	

	//逻辑线程
	Thread login=new Thread(new Runnable(){

			@Override
			public void run()
			{
				while(true){
					lx=x;
					ly=半径;
					lz=z;
					cx=x+(float)Math.sin(Cangle*Math.PI/180)*3;
					cy=4;
					cz=z+(float)Math.cos(Cangle*Math.PI/180)*3;
						if(shang){
							move(1);
						}
						if(xia){
							move(2);
						}
						if(zuo){
							move(3);
						}
						if(you){
							move(4);
						}
					try
					{
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{}
				}
			}
			
		  
	  });
	
	  //移动
	public void move(int type){
		switch(type){
			case 1:
				//前进
				z-=(float)Math.cos(Cangle*Math.PI/180)*速度;
				x-=(float)Math.sin(Cangle*Math.PI/180)*速度;
			    		
				break;
				
		    case 2:
				//退后
				z+=(float)Math.cos(Cangle*Math.PI/180)*速度;
				x+=(float)Math.sin(Cangle*Math.PI/180)*速度;
				break;
			//左
		    case 3:
				z-=(float)Math.cos((Cangle+90)*Math.PI/180)*速度;
				x-=(float)Math.sin((Cangle+90)*Math.PI/180)*速度;
				
				break;
			//右
			case 4:
				z+=(float)Math.cos((Cangle+90)*Math.PI/180)*速度;
				x+=(float)Math.sin((Cangle+90)*Math.PI/180)*速度;
				break;
			
		}
	}
	
	  
	  
	  
	private int[][] initMapData(int height, int width)
	{
		vertexList=new ArrayList<Float>();
		texcoorList=new ArrayList<Float>();
		normalList=new ArrayList<Float>();
		//设置地图的 宽度(z)和长度(x)
		int [][]mapData=null;
		
		Maze maze=new Maze(height/2,width/2);
		maze.traversal();
		maze.create();
		mapData=maze.getMaze();
		
		//把球放在原点
		//二维数组迷宫开始点在z1 x1 然后我们要转换成世界坐标
		//2是二维数组的角标好像是这样说的 
		//就是开始点在二维数组里面的[1][1]里面
        //然后转换成球的坐标就是
		setBallXYZ(2*blockWidth,2*blockWidth);
		return mapData;
	}

	//设置球的坐标
	public void setBallXYZ(float z,float x){
		this.z=z;
		this.x=x;
	}
	
	//根据二维数组生成地图顶点
	private void initVertex(){
		
		for(int z=0;z<map.length;z++){
			for(int x=0;x<map[0].length;x++){
			
				int data=getData(z,x);
				//这个块是墙的话就加面
				if(data==QIANG){
				//前面没有墙，加这个面
				if(getData(z+1,x)==KONGQI){
					addMian(0,x,z);
				}
				//右没有墙，加这个面
				if(getData(z,x+1)==KONGQI){
				addMian(1,x,z);
				}
				//后没有墙，加这个面
				if(getData(z-1,x)==KONGQI){
				addMian(2,x,z);
				}
				//左没有墙，加这个面
				if(getData(z,x-1)==KONGQI){
					addMian(3,x,z);
				}
				//不需要判定直接加入上面
		        	addMian(4,x,z);
				}
			}
		}
		
		//把集合的数据转换到数组里面
		mapVertex=new float[vertexList.size()];
		for(int i=0,size=vertexList.size();i<size;i++){
			mapVertex[i]=vertexList.get(i);
		}
		mapTexcoor=new float[texcoorList.size()];
		for(int i=0,size=texcoorList.size();i<size;i++){
			mapTexcoor[i]=texcoorList.get(i);
		}
		mapNormal=new float[normalList.size()];
		for(int i=0,size=normalList.size();i<size;i++){
			mapNormal[i]=normalList.get(i);
		}
		
		//释放内存
		vertexList=null;
		texcoorList=null;
		normalList=null;
	} 
	
	//增加顶点数组
	//0是前面 1是右边 2是后面 3是左边 4是上面 5是下面
	
	private void addMian(int size,int x,int z){
		float xx=x*blockWidth*2f;
		float zz=z*blockWidth*2f;
		switch(size){
			//前面的正方形
			case 0:
			add(-blockWidth+xx,blockWidth,blockWidth+zz);
			add(-blockWidth+xx,-blockWidth,blockWidth+zz);
			add(blockWidth+xx,-blockWidth,blockWidth+zz);
			
			add(blockWidth+xx,blockWidth,blockWidth+zz);
			add(-blockWidth+xx,blockWidth,blockWidth+zz);
			add(blockWidth+xx,-blockWidth,blockWidth+zz);
			
			//前面的法线
			addNormal(0,0,1);
			addNormal(0,0,1);
			addNormal(0,0,1);
			
			addNormal(0,0,1);
			addNormal(0,0,1);
			addNormal(0,0,1);
			//加入正面的纹理坐标
			addTexCoor(0);
			break;
			
				//右的正方形
			case 1:
				add(blockWidth+xx,blockWidth,blockWidth+zz);
				add(blockWidth+xx,-blockWidth,blockWidth+zz);
				add(blockWidth+xx,-blockWidth,-blockWidth+zz);

				add(blockWidth+xx,blockWidth,-blockWidth+zz);
				add(blockWidth+xx,blockWidth,blockWidth+zz);
				add(blockWidth+xx,-blockWidth,-blockWidth+zz);
				
				//右边的法线
				addNormal(1,0,0);
				addNormal(1,0,0);
				addNormal(1,0,0);

				addNormal(1,0,0);
				addNormal(1,0,0);
				addNormal(1,0,0);
				
				addTexCoor(0);
				break;
				
				//后的正方形 
				
			case 2:
				add(-blockWidth+xx,blockWidth,-blockWidth+zz);
				add(blockWidth+xx,-blockWidth,-blockWidth+zz);
				add(-blockWidth+xx,-blockWidth,-blockWidth+zz);
				
				add(blockWidth+xx,blockWidth,-blockWidth+zz);
				add(blockWidth+xx,-blockWidth,-blockWidth+zz);
				add(-blockWidth+xx,blockWidth,-blockWidth+zz);
				
				//后面的法线
				addNormal(0,0,-1);
				addNormal(0,0,-1);
				addNormal(0,0,-1);

				addNormal(0,0,-1);
				addNormal(0,0,-1);
				addNormal(0,0,-1);
				
				addTexCoor(1);
				break;
				
				//左的正方形 
			case 3:
				add(-blockWidth+xx,blockWidth,blockWidth+zz);
				add(-blockWidth+xx,-blockWidth,-blockWidth+zz);
				add(-blockWidth+xx,-blockWidth,blockWidth+zz);
				
				add(-blockWidth+xx,blockWidth,-blockWidth+zz);
				add(-blockWidth+xx,-blockWidth,-blockWidth+zz);
				add(-blockWidth+xx,blockWidth,blockWidth+zz);
				
				
				//左边的法线
				addNormal(-1,0,0);
				addNormal(-1,0,0);
				addNormal(-1,0,0);

				addNormal(-1,0,0);
				addNormal(-1,0,0);
				addNormal(-1,0,0);
				
				addTexCoor(1);
				break;
			//上面的正方形
			case 4:
				add(-blockWidth+xx,blockWidth,-blockWidth+zz);
				add(-blockWidth+xx,blockWidth,blockWidth+zz);
				add(blockWidth+xx,blockWidth,blockWidth+zz);
				
				add(blockWidth+xx,blockWidth,-blockWidth+zz);
				add(-blockWidth+xx,blockWidth,-blockWidth+zz);
				add(blockWidth+xx,blockWidth,blockWidth+zz);
				
				//上面的法线
				addNormal(0,1,0);
				addNormal(0,1,0);
				addNormal(0,1,0);

				addNormal(0,1,0);
				addNormal(0,1,0);
				addNormal(0,1,0);
				
				addTexCoor(0);
				break;
		}
		
		

		
		
	}
	
	//因为用到背面裁剪所以把顺时针和逆时针给分开
	public void addTexCoor(int type){
		switch(type){
			//正面 顺时针
			case 0:
				//墙用同一个纹理所以固定
				texcoorList.add(0f);
				texcoorList.add(0f);

				texcoorList.add(0f);
				texcoorList.add(1f);

				texcoorList.add(1f);
				texcoorList.add(1f);



				texcoorList.add(1f);
				texcoorList.add(0f);

				texcoorList.add(0f);
				texcoorList.add(0f);

				texcoorList.add(1f);
				texcoorList.add(1f);
			break;
			//后面 逆时针
			case 1:
				//墙用同一个纹理所以固定
				texcoorList.add(0f);
				texcoorList.add(0f);

				texcoorList.add(1f);
				texcoorList.add(1f);
				
				texcoorList.add(0f);
				texcoorList.add(1f);


				texcoorList.add(1f);
				texcoorList.add(0f);

				texcoorList.add(1f);
				texcoorList.add(1f);
				
				texcoorList.add(0f);
				texcoorList.add(0f);
				break;
		}
	}
	
	private void add(float x,float y,float z){

		vertexList.add(x);
		vertexList.add(y);
		vertexList.add(z);
		
	}
	
	private void addNormal(float x,float y,float z){
		normalList.add(x);
		normalList.add(y);
		normalList.add(z);
	}
	
	//获取这个坐标的地图数据
	public int getData(float z,float x){
		
		int data=KONGQI;
		//捕捉异常，有时候输入的x，z数据超过了数组所以会异常闪退
		//现在我们捕捉这个异常，如果超过了这个数组，那说明这个是空气
		//把坐标四舍五入
		int xx=(int)Math.floor(x);
		int zz=(int)Math.floor(z);
		Log.e("a","x"+xx+"z"+zz);
		
		try{
	 data=map[zz][xx];
	}catch(Exception e){
		data=KONGQI;
	}
		return data;
	}
	
	
	
	
	private void init()
	{
		
		renderer = new DrawRenderer(){

		
			@Override
			public void draw(int error,WLoader loader)
			{
				
			       WorldMatrix.setCamera(cx,cy,cz,lx,ly,lz,0,1,0);
				
				//设置球的原点坐标，球的原点坐标在球中心 把y提高半径
				//不然会有一半陷入地板里面
				//0.5是因为地板下降了0.5
				ball.setXYZ(x,半径-0.5f,z);
		
				}

			@Override
			public void SurfaceCreated(WLoader loader, EGLConfig p2)
			{
				//开启背面裁剪
				word.setCullFace(true);
				//获取木板纹理
				int muban=Texture.getAssetsPathTextureId(
				gamerun.getContext(),
				"muban.jpg",
				Texture.TEXTURE_TYPE_NEAREST);
				//获取草地纹理
				int cao=Texture.getAssetsPathTextureId(
				gamerun.getContext(),
				"caodi.jpg",
				Texture.TEXTURE_TYPE_NEAREST);
				//获取金属纹理
				int ballTexture=Texture.getAssetsPathTextureId(
				gamerun.getContext(),
				"jinshu.jpg",
				Texture.TEXTURE_TYPE_NEAREST);
				float vertexD[]=null;
				float texcoorD[]=null;
				//循环生成三角形顶点然后储存在ArrayList集合里面
				ArrayList<Float> vertexDiban=new ArrayList<Float>();
				//循环生成三角形顶点然后储存在ArrayList集合里面
				ArrayList<Float> texcoorDiban=new ArrayList<Float>();
				
				for(int z=0;z<map.length;z++){
					for(int x=0;x<map[0].length;x++){
						//第一个三角形
						vertexDiban.add(x*blockWidth*2);
						vertexDiban.add(0f);
						vertexDiban.add(z*blockWidth*2);				 
						vertexDiban.add(x*blockWidth*2);
						vertexDiban.add(0f);
						vertexDiban.add(z*blockWidth*2+blockWidth*2);
						vertexDiban.add(x*blockWidth*2+blockWidth*2);
						vertexDiban.add(0f);
						vertexDiban.add(z*blockWidth*2+blockWidth*2);

						//第二个三角形
						vertexDiban.add(x*blockWidth*2+blockWidth*2);
						vertexDiban.add(0f);
						vertexDiban.add(z*blockWidth*2);
						vertexDiban.add(x*blockWidth*2);
						vertexDiban.add(0f);
						vertexDiban.add(z*blockWidth*2);
						vertexDiban.add(x*blockWidth*2+blockWidth*2);
						vertexDiban.add(0f);
						vertexDiban.add(z*blockWidth*2+blockWidth*2);
						
						texcoorDiban.add(0f);
						texcoorDiban.add(0f);

						texcoorDiban.add(0f);
						texcoorDiban.add(1f);

						texcoorDiban.add(1f);
						texcoorDiban.add(1f);



						texcoorDiban.add(1f);
						texcoorDiban.add(0f);

						texcoorDiban.add(0f);
						texcoorDiban.add(0f);

						texcoorDiban.add(1f);
						texcoorDiban.add(1f);
					}
				}
				vertexD=new float[vertexDiban.size()];
				for(int i=0,size=vertexDiban.size();i<size;i++){
					//把集合顶点放在数组里面
					vertexD[i]=vertexDiban.get(i);
				}
               
				texcoorD=new float[texcoorDiban.size()];
				for(int i=0,size=texcoorDiban.size();i<size;i++){
					//把集合纹理集合放进数组里
					texcoorD[i]=texcoorDiban.get(i);
				}
				
				//初始化地板网格
				WMesh dibanMesh=new WMesh(vertexD);
				//为地板网格设置纹理坐标数组
				dibanMesh.setTexCoor(texcoorD);
				//初始化地板几何体
				dibanGeometry=new TextureGeometry(gamerun,dibanMesh);
				//设置纹理
				dibanGeometry.setTexture(cao);
				//设置地板阴影模式为 只接受阴影不产生阴影
				//显示效果不变但是性能提高了
				dibanGeometry.setDrawShadowMode(Geometry.SHADOW_MODE_ACCEPT);
				//设置原点坐标
				dibanGeometry.setXYZ(0,-0.5f,0);
				//设置地板几何体不需要光照
				dibanGeometry.setDrawLight(Geometry.LIGHT_FALSE);
				WMesh mapMesh=new WMesh(mapVertex);
				//设置地图纹理坐标
				mapMesh.setTexCoor(mapTexcoor);
				//设置地图法线
				mapMesh.setNormal(mapNormal);
				//设置地图环境光
				mapMesh.setAmbient(new ColorTool(0.5f));
				//设置地图漫反射
				mapMesh.setDiffuse(new ColorTool(0.5f));
				//初始化地图几何体
				mapGeometry=new TextureGeometry(gamerun,mapMesh);
				//设置纹理
				mapGeometry.setTexture(muban);
				//jar库自带球
				WMesh ballMesh=MeshType.getBall(半径);
				//因为用到了光照所以必须设置材质
				//否则会是一个黑球
				//设置环境光
				ballMesh.setAmbient(new ColorTool(0.5f));
				//漫反射光
				ballMesh.setDiffuse(new ColorTool(0.5f));
				//镜面光
				ballMesh.setSpecular(new ColorTool(0.5f));
				//光滑度
				ballMesh.setShininess(100);
				//初始化球几何体
				ball=new TextureGeometry(gamerun,ballMesh);
				//设置球纹理
				ball.setTexture(ballTexture);
				//把球几何体加入渲染列表
				loader.addObject(ball);
				//把地图几何体加入渲染列表
				loader.addObject(mapGeometry);
				//把地板几何体加入渲染列表
				loader.addObject(dibanGeometry);
				
			}


		};
	
}


	private void initView()
	{
		// TODO: Implement this method
		gamerun=(GameRun)findViewById(R.id.game);
		btS=(Button)findViewById(R.id.s);
		btX=(Button)findViewById(R.id.x);
		btZ=(Button)findViewById(R.id.z);
		btY=(Button)findViewById(R.id.y);

		btS.setOnTouchListener(new OnTouchListener(){

				@Override
				public boolean onTouch(View p1, MotionEvent p2)
				{
					int action=p2.getAction();
					//按钮点下
					if(action==MotionEvent.ACTION_DOWN){
						shang=true;
					}else if(action==MotionEvent.ACTION_UP){
						shang=false;
					}
					return false;
				}
				
			
		});

		btX.setOnTouchListener(new OnTouchListener(){

				@Override
				public boolean onTouch(View p1, MotionEvent p2)
				{
					int action=p2.getAction();
					//按钮点下
					if(action==MotionEvent.ACTION_DOWN){
						xia=true;
					}else if(action==MotionEvent.ACTION_UP){
						xia=false;
					}
					return false;
				}


			});
			
		btZ.setOnTouchListener(new OnTouchListener(){

				@Override
				public boolean onTouch(View p1, MotionEvent p2)
				{
					int action=p2.getAction();
					//按钮点下
					if(action==MotionEvent.ACTION_DOWN){
						zuo=true;
					}else if(action==MotionEvent.ACTION_UP){
						zuo=false;
					}
					return false;
				}


			});
			
		btY.setOnTouchListener(new OnTouchListener(){

				@Override
				public boolean onTouch(View p1, MotionEvent p2)
				{
					int action=p2.getAction();
					//按钮点下
					if(action==MotionEvent.ACTION_DOWN){
						you=true;
					}else if(action==MotionEvent.ACTION_UP){
						you=false;
					}
					return false;
				}


			});
			

		gamerun.setOnTouchListener(new OnTouchListener(){
				
				private float PreviousX;
				@Override
				public boolean onTouch(View p1, MotionEvent e)
				{
					float x = e.getX();
					switch (e.getAction()) {
						case MotionEvent.ACTION_MOVE:
							float dx = x - PreviousX;
							Cangle+=dx*0.3f;
							
					}
					
					PreviousX = x;


					return true;
				}


			});


	}





}
