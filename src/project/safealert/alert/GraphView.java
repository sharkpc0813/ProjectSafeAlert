package project.safealert.alert;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

/**
 * GraphView creates a scaled line or bar graph with x and y axis labels. 
 * @author Arno den Hond
 *
 */
public class GraphView extends View {

	public static boolean BAR = true;
	public static boolean LINE = false;

	private Paint paint;
	private float[] values;
	private String title= "";
	private boolean type;
	private int graphCnt;
	
	public GraphView(Context context){
		super(context);
		values = new float[0];
		paint = new Paint();
		type=false;
		graphCnt=0;
	}
	
	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		values = new float[0];
		paint = new Paint();
		type=false;
		graphCnt=0;
	}

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		values = new float[0];
		paint = new Paint();
		type=false;
		graphCnt=0;
	}

	public void setGraphView(float[] values,int graphCnt,boolean type) {
		if (values == null)
			values = new float[0];
		else
			this.values = values;
		this.graphCnt=graphCnt;
		this.type = type;		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		float border = 20;
		float horstart = border * 2;
		float height = getHeight();
		float width = getWidth() - 1;
		float max = getMax();
		float min = getMin();
		float diff = max - min;
		float graphheight = height - (2 * border);
		float graphwidth = width - (2 * border);
		//Log.d("Gyro","max()"+max);
		//Log.d("Gyro","graphCnt()"+graphCnt);
		paint.setTextAlign(Align.LEFT);		
		int vers = graphCnt - 1;	
		float maxTmp=0,yAxis=(float) Math.ceil(Math.ceil(max)/(graphCnt));
		if(yAxis==0)
			yAxis=(float) (Math.ceil(max)/(graphCnt));
		yAxis=1;
		//Log.d("Gyro","yAxis()"+yAxis);
		for (int i = 0; i < graphCnt; i++) {
			paint.setColor(Color.DKGRAY);
			float y = ((graphheight / vers) * i) + border;
			canvas.drawLine(horstart, y, width, y, paint);
			paint.setColor(Color.BLACK);
			if(i!=0)
				canvas.drawText(Float.toString(maxTmp-yAxis*(i)), 0, y, paint);
			else
				canvas.drawText(Float.toString(max), 0, y, paint);		
				maxTmp=(float) Math.ceil(max);
		}
		
		int hors = graphCnt - 1;
		for (int i = 0; i < graphCnt; i++) {
			paint.setColor(Color.DKGRAY);
			float x = ((graphwidth / hors) * i) + horstart;
			canvas.drawLine(x, height - border, x, border, paint);
			paint.setTextAlign(Align.CENTER);
			if (i==graphCnt-1)
				paint.setTextAlign(Align.RIGHT);
			if (i==0)
				paint.setTextAlign(Align.LEFT);
			paint.setColor(Color.BLACK);
			canvas.drawText(Integer.toString(i+1), x, height - 4, paint);
		}

		paint.setTextAlign(Align.CENTER);
		canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);

		if (max != min) {
			paint.setColor(Color.RED);
			if (type == BAR) {
				float datalength = graphCnt;
				float colwidth = (width - (2 * border)) / datalength;
				for (int i = 0; i < graphCnt; i++) {
					float val = values[i] - min;
					float rat = val / diff;
					float h = graphheight * rat;
					canvas.drawRect((i * colwidth) + horstart, 
							(border - h) + graphheight, 
							((i * colwidth) + horstart) + (colwidth - 1), 
							height - (border - 1), paint);
				}
			} else {
				float datalength = graphCnt;
				float colwidth = (width - (2 * border)) / datalength;
				float halfcol = colwidth / 2;
				float lasth = 0;
				for (int i = 0; i < graphCnt; i++) {
					float val = values[i] - min;
					float rat = val / diff;
					float h = graphheight * rat;
					if (i > 0)
						canvas.drawLine(((i - 1) * colwidth) + (horstart + 1) + halfcol, 
								(border - lasth) + graphheight, 
								(i * colwidth) + (horstart + 1) + halfcol, 
								(border - h) + graphheight, paint);
					lasth = h;
				}
			}
		}
	}

	public float getMax() {
		float largest = Integer.MIN_VALUE;
		for (int i = 0; i < graphCnt; i++)
			if (values[i] > largest)
				largest = values[i];
		return largest;
	}

	public float getMin() {
		float smallest = Integer.MAX_VALUE;
		for (int i = 0; i < graphCnt; i++)
			if (values[i] < smallest)
				smallest = values[i];
		return smallest;
	}

}
