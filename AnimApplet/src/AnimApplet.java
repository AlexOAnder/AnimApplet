

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by AlexOAnder.
 * Date: 28/10/11
 * Time: 09:51
 */
public class AnimApplet extends JApplet {

    Random r = new Random();

    public AnimApplet() {
        System.out.println("Constructor");
    }

    public class RainDrop{
        public int startPointX,startPointY,endPointX,endPointY;
        double angle;
        public int tmpLength,length;
        boolean endOfLife =false;
        Color color;

        public RainDrop(int x1,int y1,int x2,int y2,int l,double a)
        {
            // for rain zone - (x1,y1) to (x2,y2)
            startPointX = x1+r.nextInt(Math.abs(x2-x1));
            startPointY = y1+r.nextInt(Math.abs(y2-y1));
            angle = a;
            length = l;
        }

        public void slide()
        {
            endPointX = (int) (startPointX + Math.cos(Math.PI/180*angle)*tmpLength++);
            endPointY = (int) (startPointY - Math.sin(Math.PI/180*angle)*tmpLength++);
            if (tmpLength>=length)
                endOfLife = true;
        }
    }

    ArrayList<RainDrop> rain;
    ArrayList<RainDrop> removeRainList;
    Image offimage;
    Graphics offg;
    Area area;
    Area area2;

    double counter = 1000;

    public void init() {
        rain = new ArrayList<RainDrop>() ;
        new Thread() {

            @Override
            public void run() {
                while (true) {

                    repaint();
                    try {
                        sleep(50);
                        if (counter==0) {
                            showStatus("Bucket is full");
                            sleep(10000);
                            counter=1000;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }


        }.start();
    }

    public void paint(Graphics g) {
        // заного рисуем облако ( оно исчезнет как фон, если этого не сделать)
        area = drawSkyBallon(90,50);


        update(g);
    }

    public void update(Graphics g) {
        double tmpProcent = BigDecimal.valueOf(counter/1000).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();

        if (offimage == null) {
            offimage = createImage(getWidth(), getHeight());
            offg = offimage.getGraphics();
        }
        if (rain.size()>0)
        {
            // очищаем экран от капель, которые уже были очищены
            offg.clearRect(0,0,getWidth(),getHeight());
        }
        // берем "холст" который получился с прошлой итерации, как основу
        Graphics2D g2d = (Graphics2D)offg;
        // сглаживаем углы, чтобы рисуемые детали двигались более "гладко"
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        // переопределяем заного т.к. мы уже очистили нужные капли
        removeRainList = new ArrayList<RainDrop>() ;



        // трансформируем облако - уменьшая его зум - возьмем 1.4 от настоящего и постепенно уменьшаем
        if (tmpProcent>0.70) {
            AffineTransform at = new AffineTransform();
            at.scale(tmpProcent * 1.4, tmpProcent * 1.4);
            at.translate(70 - 100*(tmpProcent), 20 - tmpProcent * 30);
            area = area.createTransformedArea(at);
        }
        // зарисуем облако - сначала заполнение
        g2d.setPaint (new Color(96, 155, 226));
        g2d.fill ( area );
        // затем - края полученной формы ( т.к. это area все круги объединены как 1 объект union)
        g2d.setPaint ( Color.GRAY );
        g2d.draw ( area );

        g2d.setColor(new Color(96, 155, 226));
        rain.add(new RainDrop(140,100,220,190,40,-115));
        rain.add(new RainDrop(140,120,220,190,60,-115));

        for (RainDrop drop : rain){
            drop.slide();
            g2d.drawLine(drop.startPointX,drop.startPointY,drop.endPointX,drop.endPointY);
            if (drop.endOfLife)
                // если жизненный цикл капли окончен - добавляем ее на удаление
                removeRainList.add(drop);

        }
        // убираем из цикла те капли, которые уже "закончили свой путь"
        rain.removeAll(removeRainList);
        // рисуем картинку , которая получилась в итоге
        if (counter>0)
            counter--;

        showStatus("tmp ->"+tmpProcent+" -||- counter ->"+counter+"-------bucket fullnest in % = "+ (int)((1-tmpProcent)*100));
        // draw GeneralPath (polygon)

        drawWater(112,40,40,100,(1-tmpProcent));
        drawBucket(110,40,new Color(0,255,0));
        g.drawImage(offimage, 0, 0, this);

    }

    private Area drawSkyBallon(double startPosX,double startPosY)
    {
        // Рисуем облако ( из кругов разумеется )
        // standart Graphic
		/*
		g.setColor(color);
		// Рисуем облако ( из кругов разумеется )
		//левый крайний
		g.fillOval(startPosX+0,startPosY+20,70,70);
		// средний верхний
		g.fillOval(startPosX+50,startPosY+0,65,65);
		// средний нижний (сразу после большого левого)
		g.fillOval(startPosX+50,startPosY+35,55,55);
		// средний правый (следующий после предыдущего)
		g.fillOval(startPosX+85,startPosY+30,55,55);
		// третий (правый) верхний ( после большого среднего)
		g.fillOval(startPosX+98,startPosY+20,50,50);
		// маленький крайний правый
		g.fillOval(startPosX+122,startPosY+37,45,45);
		*/
        // use Graphic2d
        Area area = new Area();
        //левый крайний
        area.add ( new Area ( new Ellipse2D.Double (startPosX+0,startPosY+20,70,70) ) );
        // средний верхний
        area.add ( new Area ( new Ellipse2D.Double (startPosX+50,startPosY+0,65,65) ) );
        // средний нижний (сразу после большого левого)
        area.add ( new Area ( new Ellipse2D.Double (startPosX+50,startPosY+35,55,55) ) );
        // средний правый (следующий после предыдущего)
        area.add ( new Area ( new Ellipse2D.Double (startPosX+85,startPosY+30,55,55) ) );
        // третий (правый) верхний ( после большого среднего)
        area.add ( new Area ( new Ellipse2D.Double (startPosX+98,startPosY+20,50,50) ) );
        // маленький крайний правый
        area.add ( new Area ( new Ellipse2D.Double (startPosX+122,startPosY+37,45,45) ) );

        return area;
    }

    private void drawBucket(int startPosX,int startPosY,Color color){

        Graphics2D g = (Graphics2D)offg;
        //нарисуем стакан (ведро)
        Color tmp = g.getColor();
        g.setColor(color);
        BasicStroke pen1 = new BasicStroke(5); //толщина линии 5
        g.setStroke(pen1);
        // верхушка
        g.drawOval(startPosX+0,startPosY+210,120,20);
        //левая линия
        g.drawLine(startPosX+0,startPosY+220,startPosX+15,startPosY+320);
        //правая линия
        g.drawLine(startPosX+120,startPosY+220,startPosX+105,startPosY+320);
        //линия дна
        //g.drawLine(startPosX+15,320,startPosX+105,320);

        g.drawArc(startPosX+15, startPosY+310, 90, 20, 0, -180);
        g.fillArc(startPosX+15, startPosY+310, 90, 20, 0, -180);
        pen1 = new BasicStroke(1); //толщина линии 1
        g.setStroke(pen1);

        g.setColor(tmp);

    }

    public void drawWater(int startX,int startY,int w, int h,double proc)
    {

        int x1Points[] = {};
        int y1Points[] = {};

        if (proc >= 0.1 && proc <= 0.4)
        {
            // 20%
            x1Points = new int[] {startX+9, startX+104, startX+102,startX+15 };
            y1Points = new int[] {startY+303, startY+303, startY+320,startY+320};
        }
        if (proc >0.4 && proc <= 0.6)
        {
            // 40%
            x1Points = new int[] {startX+8, startX+107, startX+102,startX+15 };
            y1Points = new int[] {startY+283, startY+283, startY+320,startY+320};
        }
        if (proc > 0.6 && proc <= 0.8)
        {
            // 60%
            x1Points = new int[] {startX+6, startX+110, startX+102,startX+15 };
            y1Points = new int[] {startY+263, startY+263, startY+320,startY+320};
        }
        if (proc >0.8 && proc <= 0.9)
        {
            // 80%
            x1Points = new int[] {startX+4, startX+115, startX+102,startX+15 };
            y1Points = new int[] {startY+243, startY+243, startY+320,startY+320};
        }
        if(proc>0.9) {
            // 100%
            x1Points = new int[] {startX+0, startX+115, startX+102,startX+15 };
            y1Points = new int[] {startY+223, startY+223, startY+320,startY+320};
        }
        Graphics2D g = (Graphics2D)offg;

        if (proc > 0.1) { // if 0 % - nothing to paint
            GeneralPath polygon =
                    new GeneralPath(GeneralPath.WIND_EVEN_ODD,
                            x1Points.length);
            polygon.moveTo(x1Points[0], y1Points[0]);

            for (int index = 1; index < x1Points.length; index++) {
                polygon.lineTo(x1Points[index], y1Points[index]);
            }

            polygon.closePath();
            Area area4 = new Area(polygon);

            g.fill(polygon);
        }
    }
}