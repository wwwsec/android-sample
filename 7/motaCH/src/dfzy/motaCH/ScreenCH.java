package dfzy.motaCH;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.game.ManagerCH;

import dfzy.motaCH.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.KeyEvent;

public class ScreenCH extends ViewCH
{
	private Paint			paint		= null;
	public MainCH			mMainGame	= null;
	
	public static final int TEXT_COLOR = 0xffc800;
	public static final int BACK_COLOR = 0x000000;
	public static final int SMALL_FONT = 12;
	public static final int NORMAL_FONT = 15;
	public static final int LARGE_FONT = 16;
	public static final int UP = 0,DOWN = 1,LEFT = 2,RIGHT = 3;
	public static final int MILLIS_PER_TICK = 300;
	
	private ManagerCH layerManager;
	private boolean mshowMessage = false;
	public boolean mshowDialog = false;
	public boolean mshowFight = false;
	private String strMessage = "";
	public FightScreen mFightScreen;
	
	private HeroCH hero;
	private MapCH gameMap;
	private FightCH fightCalc;
	private TaskCH task;
	private static final int step = MapCH.TILE_WIDTH;
	public Canvas mcanvas;
	public static final int IMAGE_HERO = 0,
	IMAGE_MAP = 1,
		IMAGE_DIALOG_HERO = 2,
	IMAGE_DIALOG_ANGLE = 3,
	IMAGE_DIALOG_THIEF = 4,
	IMAGE_BORDER = 5,
	IMAGE_DIALOGBOX = 6,
	IMAGE_MESSAGEBOX = 7,
	IMAGE_BORDER2 = 8,
	IMAGE_BORDER3 = 9,
	IMAGE_BORDER4 = 10,
	IMAGE_DIALOG_PRINCESS = 11,
	IMAGE_DIALOG_BOSS = 12,
	IMAGE_BLUE_GEEZER = 13,
	IMAGE_RED_GEEZER = 14,
	IMAGE_SPLASH = 15,
	IMAGE_GAMEOVER = 16;
	
	public int					borderX, borderY;
	private int				winWidth, winHeight;
	private int				scrollX, scrollY;
	private int				curDialogImg;
	private MagicCH			magicTower;

	public TextCH				tu				= null;
	private int				miType			= -1;
	
	public ScreenCH(Context context, MagicCH magicTower, MainCH mainGame, boolean isNewGame)
	{
		super(context);
		mMainGame = mainGame;
		paint = new Paint();
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		this.magicTower = magicTower;
		winWidth = yaCH.BORDERW;
		winHeight = yaCH.BORDERH;
		borderX = yaCH.BORDERX;
		borderY = yaCH.SCREENH - yaCH.BORDERH - 60;
		layerManager = new ManagerCH();
		tu = new TextCH();
		hero = new HeroCH(BitmapFactory.decodeResource(this.getResources(), R.drawable.hero16), MapCH.TILE_WIDTH, MapCH.TILE_HEIGHT);
		hero.defineReferencePixel(MapCH.TILE_WIDTH / 2, MapCH.TILE_HEIGHT / 2);
		gameMap = new MapCH(hero, BitmapFactory.decodeResource(this.getResources(), R.drawable.map16));
		fightCalc = new FightCH(hero);
		task = new TaskCH(this, hero, gameMap);
		hero.setTask(task);
		if (isNewGame == false)
		{
			load();
		}
		layerManager.append(hero);
		layerManager.append(gameMap.getFloorMap());
	}


	protected void onDraw(Canvas canvas)
	{
		mcanvas = canvas;
		paint.setColor(Color.BLACK);
		yaCH.fillRect(canvas, 0, 0, yaCH.SCREENW, yaCH.SCREENH, paint);

		drawAttr(canvas);
		gameMap.animateMap();
		scrollWin();
		layerManager.setViewWindow(scrollX, scrollY, winWidth, winHeight);
		layerManager.paint(canvas, borderX, borderY);

		if (mshowMessage)
		{
			showMessage(strMessage);
		}
		if (mshowDialog)
		{
			dialog();
		}
		if (mFightScreen != null && mshowFight)
		{
			mFightScreen.onDraw(canvas);
		}
	}


	public boolean onKeyUp(int keyCode)
	{
		int type = 0;
		if (keyCode == KeyEvent.KEYCODE_1)
		{
			mMainGame.mCMIDIPlayer.StopMusic();
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_2)
		{
			mMainGame.mCMIDIPlayer.PlayMusic(2);
		}
		if (mFightScreen != null && mshowFight)
		{
			mFightScreen.onKeyUp(keyCode);
			return false;
		}
		if ((mshowMessage && keyCode != yaCH.KEY_DPAD_OK) || (mshowDialog && keyCode != yaCH.KEY_DPAD_OK))
		{
			return false;
		}
		switch (keyCode)
		{
			case yaCH.KEY_SOFT_RIGHT:
				save();
				mMainGame.controlView(yaCH.GAME_MENU);
				if (mMainGame.mbMusic == 1)
				{
					mMainGame.mCMIDIPlayer.PlayMusic(1);
				}
				break;
			case yaCH.KEY_DPAD_OK:
				if (mshowMessage)
				{
					// 知道无法翻页为止
					if (!tu.Key(yaCH.KEY_DPAD_DOWN))
					{
						mshowMessage = false;
						if ((miType >= MapCH.MAP_YELLOW_KEY) && (miType <= MapCH.MAP_SHIELD3))
						{
							miType = -1;
							gameMap.remove();
						}
					}
				}
				else if (mshowDialog)
				{
					if (!tu.Key(yaCH.KEY_DPAD_DOWN))
					{
						if (task.mbtask)
						{
							if (task.curTask2 < TaskCH.finishedDialog[task.curTask].length - 1)
							{
								task.curTask2++;
								tu.InitText(TaskCH.finishedDialog[task.curTask][task.curTask2], 0, (yaCH.SCREENH - yaCH.MessageBoxH) / 2, yaCH.SCREENW,
									yaCH.MessageBoxH, 0x0, 0xffff00, 255, yaCH.TextSize);
							}
							else
							{
								task.curTask2 = 0;
								mshowDialog = false;
							}
						}
						else
						{
							if (task.curTask2 < TaskCH.recieveDialog[task.curTask].length - 1)
							{
								task.curTask2++;
								tu.InitText(TaskCH.recieveDialog[task.curTask][task.curTask2], 0, (yaCH.SCREENH - yaCH.MessageBoxH) / 2, yaCH.SCREENW,
									yaCH.MessageBoxH, 0x0, 0xffff00, 255, yaCH.TextSize);
							}
							else
							{
								task.curTask2 = 0;
								mshowDialog = false;
							}
						}
					}
				}
				break;
			case yaCH.KEY_DPAD_UP:
				hero.setFrame(9);
				if ((type = gameMap.canPass(UP)) == 1)
				{
					hero.move(0, -step);
				}
				break;
			case yaCH.KEY_DPAD_DOWN:
				hero.setFrame(0);
				if ((type = gameMap.canPass(DOWN)) == 1)
				{
					hero.move(0, step);
				}
				break;
			case yaCH.KEY_DPAD_LEFT:
				hero.setFrame(3);
				if ((type = gameMap.canPass(LEFT)) == 1)
				{
					hero.move(-step, 0);
				}
				break;
			case yaCH.KEY_DPAD_RIGHT:
				hero.setFrame(6);
				if ((type = gameMap.canPass(RIGHT)) == 1)
				{
					hero.move(step, 0);
				}
				break;
			default:
				break;
		}
		if (type >= MapCH.MAP_LOCKED_BARRIER)
			dealType(type);
		return true;
	}


	public boolean onKeyDown(int keyCode)
	{
		return true;
	}


	public void refurbish()
	{

	}


	public void reCycle()
	{
		paint = null;
		System.gc();
	}


	private void dealType(int type)
	{
		if (type == MapCH.MAP_UPSTAIR)
		{
			gameMap.upstair();
			hero.setFrame(0);
		}
		else if (type == MapCH.MAP_DOWNSTAIR)
		{
			gameMap.downstair();
			hero.setFrame(0);
		}
		else if ((type >= MapCH.MAP_YELLOW_DOOR) && (type <= MapCH.MAP_RED_DOOR))
		{
			if (hero.useKey(type))
			{
				gameMap.remove();
			}
		}
		else if (type == MapCH.MAP_BARRIER)
		{
			gameMap.remove();
		}
		else if ((type == MapCH.MAP_SHOP1) || (type == MapCH.MAP_SHOP2))
		{
			if (gameMap.curFloorNum == 3)
			{
				// shop(ShopScreen.SHOP_3);
			}
			else
			{
				// shop(ShopScreen.SHOP_11);
			}
		}
		else if ((type >= MapCH.MAP_YELLOW_KEY) && (type <= MapCH.MAP_SHIELD3))
		{
			mshowMessage = true;
			miType = type;
			tu.InitText(hero.takeGem(type), 0, (yaCH.SCREENH - yaCH.MessageBoxH) / 2, yaCH.SCREENW, yaCH.MessageBoxH, 0x0, 0xff0000, 255, yaCH.TextSize);
		}
		else if (type >= MapCH.MAP_ANGLE)
		{
			if (type > MapCH.MAP_ORGE31)
				type -= MapCH.SWITCH_OFFSET;
			if (((type >= MapCH.MAP_ANGLE) && (type <= MapCH.MAP_RED_GEEZER)) || (type == MapCH.MAP_ORGE31))
			{
				/* 处理任务，注意不同层的老头的任务不同 */
				dealTask(type);
			}
			else if ((type >= MapCH.MAP_ORGE1) && (type <= MapCH.MAP_ORGE30))
			{
				fight(type);
			}
		}
	}


	public void showMessage(String msg)
	{
		int w = yaCH.SCREENW;
		int h = yaCH.MessageBoxH;
		int x = 0;
		int y = (yaCH.SCREENH - yaCH.MessageBoxH) / 2;
		Paint ptmPaint = new Paint();
		ptmPaint.setARGB(255, Color.red(BACK_COLOR), Color.green(BACK_COLOR), Color.blue(BACK_COLOR));

		yaCH.fillRect(mcanvas, x, y, w, h, ptmPaint);

		tu.DrawText(mcanvas);
		ptmPaint = null;
	}


	private void drawAttr(Canvas canvas)
	{
		int iH = 17;
		Paint ptmPaint = new Paint();
		ptmPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		ptmPaint.setARGB(255, Color.red(0xffff00), Color.green(0xffff00), Color.blue(0xffff00));

		yaCH.drawRect(canvas, 0, 420, 320, 60, ptmPaint);
		canvas.drawLine(70, 420, 70, 480, ptmPaint);
		canvas.drawLine(230, 420, 230, 480, ptmPaint);

		yaCH.drawRect(canvas, 0, 0, 320, 68, ptmPaint);

		yaCH.drawImage(canvas, getImage(IMAGE_HERO), 19, 424, 32, 32, 0, 0);

		ptmPaint.setTextSize(ScreenCH.SMALL_FONT);
		ptmPaint.setARGB(255, Color.red(0xffffff), Color.green(0xffffff), Color.blue(0xffffff));
		// 等级
		yaCH.drawString(canvas, "等级：" + hero.getLevel(), 70, 425, ptmPaint);
		yaCH.drawString(canvas, "经验：" + hero.getExperience(), 70, 425 + 1 * iH, ptmPaint);
		yaCH.drawString(canvas, "金币：" + hero.getMoney(), 70, 425 + 2 * +iH, ptmPaint);

		yaCH.drawString(canvas, "生命：" + hero.getHp(), 150, 425, ptmPaint);
		yaCH.drawString(canvas, "攻击：" + hero.getAttack(), 150, 425 + 1 * iH, ptmPaint);
		yaCH.drawString(canvas, "防御：" + hero.getDefend(), 150, 425 + 2 * +iH, ptmPaint);

		yaCH.drawString(canvas, "红钥匙：" + hero.getRedKey(), 230, 425, ptmPaint);
		yaCH.drawString(canvas, "黄钥匙：" + hero.getYellowKey(), 230, 425 + 1 * iH, ptmPaint);
		yaCH.drawString(canvas, "蓝钥匙：" + hero.getBlueKey(), 230, 425 + 2 * +iH, ptmPaint);

		String string = "《序章》";
		if (gameMap.curFloorNum != 0)
		{
			string = "《第" + gameMap.curFloorNum + "层》";
		}
		yaCH.drawString(canvas, string, (70 - ptmPaint.measureText(string)) / 2, 460, ptmPaint);

		ptmPaint.setTextSize(17);
		string = "《魔塔Android版》谢谢使用！";
		yaCH.drawString(canvas, string, (yaCH.BORDERW - ptmPaint.measureText(string)) / 2, (68 - 17) / 2, ptmPaint);

		ptmPaint = null;
	}


	private void scrollWin()
	{
		scrollX = hero.getRefPixelX() - winWidth / 2;
		scrollY = hero.getRefPixelY() - winHeight / 2;
		if (scrollX < 0)
		{
			scrollX = 0;
		}
		else if ((scrollX + winWidth) > MapCH.MAP_WIDTH)
		{
			scrollX = MapCH.MAP_WIDTH - winWidth;
		}
		if (scrollY < 0)
		{
			scrollY = 0;
		}
		else if ((scrollY + winHeight) > MapCH.MAP_HEIGHT)
		{
			scrollY = MapCH.MAP_HEIGHT - winHeight;
		}
	}


	public boolean fight(int type)
	{
		if (fightCalc.canAttack(type) == false)
			return false;
		mFightScreen = new FightScreen(this, fightCalc, hero, type);
		mshowFight = true;
		gameMap.remove();
		return true;
	}


	private void shop(int type)
	{
		// 商店
	}


	private void jump()
	{
		// 跳
	}


	private void lookup()
	{
		// 查看
	}


	private void dealTask(int type)
	{
		int curTask = -1;
		switch (type)
		{
			case MapCH.MAP_ANGLE:
				curTask = TaskCH.FIND_CROSS;
				curDialogImg = IMAGE_DIALOG_ANGLE;
				break;
			case MapCH.MAP_THIEF:
				curTask = TaskCH.FIND_AX;
				curDialogImg = IMAGE_DIALOG_THIEF;
				break;
			case MapCH.MAP_PRINCESS:
				curTask = TaskCH.RESCUE_PRINCESS;
				curDialogImg = IMAGE_DIALOG_PRINCESS;
				break;
			case MapCH.MAP_BLUE_GEEZER:
				switch (gameMap.curFloorNum)
				{
					case 2:
						curTask = TaskCH.GET_QINGFEND_JIAN;
						curDialogImg = IMAGE_BLUE_GEEZER;
						mshowMessage = true;
						miType = type;
						tu.InitText(hero.takeGem(MapCH.MAP_SWORD3), 0, (yaCH.SCREENH - yaCH.MessageBoxH) / 2, yaCH.SCREENW, yaCH.MessageBoxH, 0x0,
							0xff0000, 255, yaCH.TextSize);
						// showMessage(mcanvas,hero.takeGem(GameMap.MAP_SWORD3));
						break;
					case 5:
						// shop(ShopScreen.SHOP_5_1);
						break;
					case 13:
						// shop(ShopScreen.SHOP_13);
						break;
					case 15:
						curTask = TaskCH.GET_SHENGGUANG_JIAN;
						curDialogImg = IMAGE_BLUE_GEEZER;
						break;
				}
				break;
			case MapCH.MAP_RED_GEEZER:
				switch (gameMap.curFloorNum)
				{
					case 2:
						curTask = TaskCH.GET_HUANGJIN_DUN;
						curDialogImg = IMAGE_RED_GEEZER;
						mshowMessage = true;
						miType = type;
						tu.InitText(hero.takeGem(MapCH.MAP_SHIELD3), 0, (yaCH.SCREENH - yaCH.MessageBoxH) / 2, yaCH.SCREENW, yaCH.MessageBoxH, 0x0,
							0xff0000, 255, yaCH.TextSize);
						// showMessage(mcanvas,hero.takeGem(GameMap.MAP_SHIELD3));
						break;
					case 5:
						// shop(ShopScreen.SHOP_5_2);
						break;
					case 12:
						// shop(ShopScreen.SHOP_12);
						break;
					case 15:
						curTask = TaskCH.GET_XINGGUANG_DUN;
						curDialogImg = IMAGE_RED_GEEZER;
						break;
				}
				break;
			case MapCH.MAP_ORGE31:
				curTask = TaskCH.FIGHT_BOSS;
				curDialogImg = IMAGE_DIALOG_BOSS;
				break;
		}
		if (curTask == -1)
			return;
		task.execTask(curTask);
	}


	public void dialog()
	{
		int x, y, w, h;
		w = yaCH.SCREENW;
		h = yaCH.MessageBoxH;
		x = 0;
		y = (yaCH.SCREENH - yaCH.MessageBoxH) / 2;

		if (task.curTask2 % 2 == 0)
		{
			drawDialogBox(IMAGE_DIALOG_HERO, x, y, w, h);
		}
		else
		{
			drawDialogBox(curDialogImg, x, y, w, h);
		}

		tu.DrawText(mcanvas);
	}


	private void drawDialogBox(int imgType, int x, int y, int w, int h)
	{
		Paint ptmPaint = new Paint();
		ptmPaint.setARGB(255, Color.red(BACK_COLOR), Color.green(BACK_COLOR), Color.blue(BACK_COLOR));

		yaCH.fillRect(mcanvas, x, y, w, h, ptmPaint);

		Bitmap img = getImage(imgType);

		yaCH.drawRect(mcanvas, x, y, w, h, ptmPaint);
		if (img != null)
		{
			if (imgType == IMAGE_DIALOG_HERO)
			{
				yaCH.drawImage(mcanvas, img, x, y - 64);
			}
			else
			{
				yaCH.drawImage(mcanvas, img, yaCH.SCREENW - 40, y - 64);
			}
		}
		ptmPaint = null;
	}


	protected void keyPressed(int keyCode)
	{

		// switch(keyCode){
		// case GameCanvas.KEY_NUM1: jump();break;
		// case GameCanvas.KEY_NUM3: lookup();break;
		// }
	}


	public void end()
	{
		// stop();
		// EndScreen end = new EndScreen(display,menu);
		// display.setCurrent(end);
		// end.start();
	}


	public Bitmap getImage(int type)
	{
		Bitmap result = null;
		switch (type)
		{
			case IMAGE_HERO:
				result = BitmapFactory.decodeResource(this.getResources(), R.drawable.hero16);
				break;
			case IMAGE_MAP:
				result = BitmapFactory.decodeResource(this.getResources(), R.drawable.map16);
				break;
			case IMAGE_DIALOG_HERO:
				result = BitmapFactory.decodeResource(this.getResources(), R.drawable.dialog_hero);
				break;
			case IMAGE_DIALOG_ANGLE:
				result = BitmapFactory.decodeResource(this.getResources(), R.drawable.dialog_angle);
				break;
			case IMAGE_DIALOG_THIEF:
				result = BitmapFactory.decodeResource(this.getResources(), R.drawable.dialog_thief);
				break;
			case IMAGE_DIALOG_PRINCESS:
				result = BitmapFactory.decodeResource(this.getResources(), R.drawable.dialog_princess);
				break;
			case IMAGE_DIALOG_BOSS:
				result = BitmapFactory.decodeResource(this.getResources(), R.drawable.dialog_boss);
				break;
			case IMAGE_BLUE_GEEZER:
				result = BitmapFactory.decodeResource(this.getResources(), R.drawable.dialog_bluegeezer);
				break;
			case IMAGE_RED_GEEZER:
				result = BitmapFactory.decodeResource(this.getResources(), R.drawable.dialog_redgeezer);
				break;
			case IMAGE_GAMEOVER:
				result = BitmapFactory.decodeResource(this.getResources(), R.drawable.gameover);
				break;
		}
		return result;
	}


	boolean save()
	{
		int col = hero.getRefPixelX() / MapCH.TILE_WIDTH;
		int row = hero.getRefPixelY() / MapCH.TILE_HEIGHT;
		byte[] r1 = hero.encode();
		byte[] r2 = { (byte) gameMap.curFloorNum, (byte) gameMap.reachedHighest, (byte) row, (byte) col, (byte) hero.getFrame() };
		byte[] r3 = task.getTask();

		Properties properties = new Properties();

		properties.put("music", String.valueOf(mMainGame.mbMusic));

		properties.put("r1l", String.valueOf(r1.length));
		properties.put("r2l", String.valueOf(r2.length));
		properties.put("r3l", String.valueOf(r3.length));
		for (int i = 0; i < r1.length; i++)
		{
			properties.put("r1_" + i, String.valueOf(r1[i]));
		}
		for (int i = 0; i < r2.length; i++)
		{
			properties.put("r2_" + i, String.valueOf(r2[i]));
		}
		for (int i = 0; i < r3.length; i++)
		{
			properties.put("r3_" + i, String.valueOf(r3[i]));
		}

		for (int i = 0; i < MapCH.FLOOR_NUM; i++)
		{
			byte map[] = gameMap.getFloorArray(i);
			for (int j = 0; j < map.length; j++)
			{
				properties.put("map_" + i + "_" + j, String.valueOf(map[j]));
			}
		}

		try
		{
			FileOutputStream stream = magicTower.openFileOutput("save", Context.MODE_WORLD_WRITEABLE);
			properties.store(stream, "");
		}
		catch (FileNotFoundException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}

		return true;
	}


	boolean load()
	{
		Properties properties = new Properties();
		try
		{
			FileInputStream stream = magicTower.openFileInput("save");

			properties.load(stream);
		}
		catch (FileNotFoundException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}

		mMainGame.mbMusic = Byte.valueOf(properties.get("music").toString());

		byte[] r1 = new byte[Byte.valueOf(properties.get("r1l").toString())];
		byte[] r2 = new byte[Byte.valueOf(properties.get("r2l").toString())];
		byte[] r3 = new byte[Byte.valueOf(properties.get("r3l").toString())];

		for (int i = 0; i < r1.length; i++)
		{
			r1[i] = Byte.valueOf(properties.get("r1_" + i).toString());
		}
		for (int i = 0; i < r2.length; i++)
		{
			r2[i] = Byte.valueOf(properties.get("r2_" + i).toString());
		}
		for (int i = 0; i < r3.length; i++)
		{
			r3[i] = Byte.valueOf(properties.get("r3_" + i).toString());
		}

		hero.decode(r1);
		gameMap.curFloorNum = r2[0];
		gameMap.reachedHighest = r2[1];
		hero.setFrame(r2[4]);
		task.setTask(r3);

		for (int i = 0; i < MapCH.FLOOR_NUM; i++)
		{
			byte[] map = new byte[MapCH.TILE_NUM];
			for (int j = 0; j < map.length; j++)
			{
				map[j] = Byte.valueOf(properties.get("map_" + i + "_" + j).toString());
			}

			gameMap.setFloorArray(i, map);
		}

		gameMap.setMap(gameMap.curFloorNum);

		hero.setRefPixelPosition(r2[3] * MapCH.TILE_WIDTH + MapCH.TILE_WIDTH / 2, r2[2] * MapCH.TILE_HEIGHT + MapCH.TILE_HEIGHT / 2);

		return true;
	}
}

