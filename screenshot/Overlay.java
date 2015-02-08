package com.unacceptableuse.screenshot;



import java.awt.MouseInfo;







import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Overlay implements KeyListener,MouseListener,MouseMoveListener,PaintListener
{
	int state = 0, x = 0, y = 0, w = 0, h = 0;
	protected Shell shell;
	Canvas canvas;
	Screenshot screenshot;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static Overlay launch(Screenshot screenshot)
	{
		Overlay o = new Overlay(screenshot);
		o.open();
		return o;
		
	}
	
	public Overlay(Screenshot screenshot)
	{
		this.screenshot = screenshot;
	}

	/**
	 * Open the window.
	 */
	public void open()
	{
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed())
		{
			if(state == 2)
			{
				screenshot.x = x;
				screenshot.y = y;
				screenshot.w = w;
				screenshot.h = h;
				shell.close();
			}
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents()
	{
		shell = new Shell(SWT.ON_TOP);
		shell.setAlpha(30);
		shell.setBackground(new Color(Display.getDefault(), 255, 255, 255));
		shell.setBounds(-1920, 0, 1920*3,1080);
		shell.setText("You should never see this");
		shell.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_CROSS));
		canvas = new Canvas(shell, SWT.ON_TOP);
		canvas.setBounds(0, 0, 1920*3,1080);

		canvas.addPaintListener(this);
		canvas.addMouseMoveListener(this);
		canvas.addMouseListener(this);
		shell.addKeyListener(this);
		canvas.setRedraw(true);
	}

	@Override
	public void paintControl(PaintEvent e)
	{
		
		e.gc.setAlpha(255);
		//e.gc.setBackground(new Color(e.display, 0, 0, 0));
		//e.gc.fillRectangle(MouseInfo.getPointerInfo().getLocation().x+16, MouseInfo.getPointerInfo().getLocation().y+16, 256, 32);
		e.gc.drawText("("+MouseInfo.getPointerInfo().getLocation().x+","+MouseInfo.getPointerInfo().getLocation().y+")", MouseInfo.getPointerInfo().getLocation().x+16+1920, MouseInfo.getPointerInfo().getLocation().y+16+1920);
		
		
		e.gc.setLineWidth(5);
		if(state == 1)
			e.gc.drawRectangle(new Rectangle(1920+x, y, (MouseInfo.getPointerInfo().getLocation().x-x), MouseInfo.getPointerInfo().getLocation().y-y));
		

			
		
	}

	@Override
	public void mouseMove(MouseEvent e)
	{
		canvas.redraw();
	}

	@Override
	public void mouseDoubleClick(MouseEvent e)
	{
	}

	@Override
	public void mouseDown(MouseEvent e)
	{
		if(state == 0)
		{
			x = e.x-1920;
			y = e.y;
			System.out.println(x+","+y);
			state = 1;
		}
	}

	@Override
	public void mouseUp(MouseEvent e)
	{
		if(state == 1)
		{
			
			w = e.x-x-1920;
			h = e.y-y;
			System.out.println(w+","+h);
			state = 2;
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if(e.keyCode == java.awt.event.KeyEvent.VK_ESCAPE)
		{
			shell.close();
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}




}
