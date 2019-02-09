/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmsc405.week.pkg5;

/**
 * File: MainClass.java
 * Author: Dillan Cobb
 * Date: 11/23/2018
 * Purpose: To create a small scene that is with 3d objects containing materials
 * and lighting.
 */

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.gl2.GLUT; 

import java.io.File;
import java.io.IOException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 *  Shows a scene (a teapot on a short cylindrical base) that is illuminated
 *  by up to four lights plus global ambient light.  The user can turn the
 *  lights on and off.  The global ambient light is a dim white.  There is 
 *  a white "viewpoint" light that points from the direction of the viewer
 *  into the scene.  There is a red light, a blue light, and a green light
 *  that rotate in circles above the teapot.  (The user can turn the animation
 *  on and off.)  The locations of the colored lights are marked by spheres,
 *  which are gray when the light is off and are colored by some emission color
 *  when the light is on.  The teapot is gray with weak specular highlights.
 *  The base is colored with a spectrum.  (The user can turn the display of
 *  the base on and off.) The mouse can be used to rotate the scene.
 */
public class MainClass extends JPanel implements GLEventListener {

    public static void main(String[] args) {
        JFrame window = new JFrame("A Lighting Demo");
        MainClass panel = new MainClass();
        window.setContentPane(panel);
        window.pack();
        window.setLocation(50,50);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    private JCheckBox animating;  // Checked if animation is running.

    private JCheckBox viewpointLight;  // Checked if the white viewpoint light is on.
    private JCheckBox purpleLight;  // Checked if the red light is on.
    private JCheckBox yellowLight;  // Checked if the green light is on.
    private JCheckBox whiteLight;  // Checked if the blue light is on.
    private JCheckBox ambientLight;  // Checked if the global ambient light is on.
    
    private JCheckBox drawBase; // Checked if the base should be drawn.

    private GLJPanel display;
    private Timer animationTimer;

    private int frameNumber = 0;  // The current frame number for an animation.

    private Camera camera;

    private GLUT glut = new GLUT();
    
    private int texture;
    private int texture2;

    /**
     * The constructor adds seven checkboxes under the display, to control the options.
     */
    public MainClass() {
        GLCapabilities caps = new GLCapabilities(null);
        display = new GLJPanel(caps);
        display.setPreferredSize( new Dimension(600,600) );
        display.addGLEventListener(this);
        setLayout(new BorderLayout());
        add(display,BorderLayout.CENTER);
        camera = new Camera();
        camera.lookAt(5,10,30, 0,0,0, 0,1,0);
        camera.setScale(15);
        camera.installTrackball(display);
        animationTimer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                frameNumber++;
                display.repaint();
            }
        });
        ActionListener boxHandler = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource() == animating) {
                    if (animating.isSelected()) {
                        animationTimer.start();
                    }
                    else {
                        animationTimer.stop();
                    }
                }
                else {
                    display.repaint();
                }
            }
        };
        viewpointLight = new JCheckBox("Viewpoint Light", true);
        purpleLight = new JCheckBox("Purple Light", true);
        whiteLight = new JCheckBox("White Light", true);
        yellowLight = new JCheckBox("Yellow Light", true);
        ambientLight = new JCheckBox("Global Ambient Light", true);
        animating = new JCheckBox("Animate", true);
        drawBase = new JCheckBox("Draw Base", true);
        viewpointLight.addActionListener(boxHandler);
        ambientLight.addActionListener(boxHandler);
        purpleLight.addActionListener(boxHandler);
        yellowLight.addActionListener(boxHandler);
        whiteLight.addActionListener(boxHandler);
        animating.addActionListener(boxHandler);
        drawBase.addActionListener(boxHandler);
        JPanel bottom = new JPanel();
        bottom.setLayout(new GridLayout(2,1));
        JPanel row1 = new JPanel();
        row1.add(animating);
        row1.add(drawBase);
        row1.add(ambientLight);
        bottom.add(row1);
        JPanel row2 = new JPanel();
        row2.add(viewpointLight);
        row2.add(purpleLight);
        row2.add(yellowLight);
        row2.add(whiteLight);
        bottom.add(row2);
        add(bottom,BorderLayout.SOUTH);
        animationTimer.setInitialDelay(500);
        animationTimer.start();
    }
    
    // ----------------------------- Methods for drawing -------------------------------

    /**
     *  Sets the positions of the colored lights and turns them on and off, depending on
     *  the state of the redLight, greenLight, and blueLight options.  Draws a small
     *  sphere at the location of each light.
     */
    private void lights(GL2 gl) {

        gl.glColor3d(0.5,0.5,0.5);
        float zero[] = { 0, 0, 0, 1 };
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, zero, 0);
        
        if (viewpointLight.isSelected())
            gl.glEnable(GL2.GL_LIGHT0);
        else
            gl.glDisable(GL2.GL_LIGHT0);
        
        if (purpleLight.isSelected()) {
            float purple[] = { 1, 0, 1, 1 };
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, purple, 0);  
            gl.glEnable(GL2.GL_LIGHT1);
        }
        else {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zero, 0);  
            gl.glDisable(GL2.GL_LIGHT1);
        }
        gl.glPushMatrix();
        gl.glRotated(-frameNumber, 0, 1, 0);
        gl.glTranslated(10, 7, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, zero, 0);
        glut.glutSolidSphere(0.5, 16, 8);
        gl.glPopMatrix();
        
        if (yellowLight.isSelected()) {
            float yellow[] = {1, 1, 0, 1 };
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, yellow, 0); 
            gl.glEnable(GL2.GL_LIGHT2);
        }
        else {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zero, 0); 
            gl.glDisable(GL2.GL_LIGHT2);
        }
        gl.glPushMatrix();
        gl.glRotated((frameNumber+100)*0.8743, 0, 1, 0);
        gl.glTranslated(9, 8, 0);
        gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_POSITION, zero, 0);
        glut.glutSolidSphere(0.5, 16, 8);
        gl.glPopMatrix();
        
        if (whiteLight.isSelected()) {
            float white[] = { 1, 1, 1, 1 };
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, white, 0); 
            gl.glEnable(GL2.GL_LIGHT3);
        }
        else {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zero, 0);
            gl.glDisable(GL2.GL_LIGHT3);
        }
        gl.glPushMatrix();
        gl.glRotated((frameNumber-100)*1.3057, 0, 1, 0);
        gl.glTranslated(9.5, 7.5, 0);
        gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_POSITION, zero, 0);
        glut.glutSolidSphere(0.5, 16, 8);
        gl.glPopMatrix();

        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zero, 0); // Turn off emission color!
    } // end lights()

    /**
     * Creates an array containing the RGBA corresponding to the specified hue, with saturation
     * 1 and brightness 0.6.  The hue should be in the range 0.0 to 1.0.
     */
    private float[] colorArrayForHue(double hue) {
        Color c = Color.getHSBColor((float)hue, 1, 0.6F);
        return new float[] { c.getRed()/255.0F, c.getGreen()/255.0F, c.getBlue()/255.0F, 1 };
    }

    /**
     *  Draws a cylinder with height 2 and radius 1, centered at the origin, with its axis
     *  along the z-axis.  A spectrum of hues is applied to the vertices along the edges
     *  of the cylinder.  (Since GL_COLOR_MATERIAL is enabled in this program, the colors
     *  specified here are used as ambient and diffuse material colors for the cylinder.)
     */
    private void drawBase(GL2 gl) {
        gl.glNormal3d( 0, 0, 1);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, texture2);
        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2d(0,0); gl.glVertex3d(0, 0, 0);
        gl.glTexCoord2d(0,1); gl.glVertex3d(1, 0, 0);
        gl.glTexCoord2d(1,1); gl.glVertex3d(1, 0, -1);
        gl.glTexCoord2d(1,0); gl.glVertex3d(0, 0, -1);
        gl.glEnd();
        gl.glFlush();
    }

    // ---------------  Methods of the GLEventListener interface -----------

    /**
     * Draws the scene.
     */
    public void display(GLAutoDrawable drawable) {    
        // called when the panel needs to be drawn

        GL2 gl = drawable.getGL().getGL2();

        gl.glClearColor(0,0,0,0);
        gl.glClear( GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT ); 

        camera.apply(gl);

        lights(gl);

        float zero[] = { 0, 0, 0, 1 };

        if (ambientLight.isSelected()) {
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, new float[] { 0.15F, 0.15F, 0.15F, 1 }, 0 );
        }
        else {
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, zero, 0 );
        }

        if (drawBase.isSelected()) {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, zero, 0 );
    
            gl.glPushMatrix();
            gl.glTranslated(-10, -5, 10);
            gl.glScaled(20,0,20);
            drawBase(gl);
            gl.glPopMatrix();
        }
        
        gl.glPushMatrix();
        gl.glTranslated(-4,-4.5,0);
        gl.glScaled(11,11,11);
        drawTower(gl);
        gl.glPopMatrix();
        
        gl.glPushMatrix();
        gl.glTranslated(-2,-4.5,0);
        gl.glScaled(9,9,9);
        drawGate(gl);
        gl.glPopMatrix();
        
        gl.glPushMatrix();
        gl.glTranslated(5,-4.5,0);
        gl.glScaled(11,11,11);
        drawTower(gl);
        gl.glPopMatrix();

        gl.glColor3d(0.7,0.7,0.7);  // sets diffuse and ambient color for teapot

        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] {0.2F, 0.2F, 0.2F, 1 }, 0);
    }

    /**
     * Initialization, including setting up a camera and configuring the four lights.
     */
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0, 0, 0, 1);  
        gl.glEnable(GL2.GL_DEPTH_TEST); 
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, 1);
        gl.glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 32);
        gl.glShadeModel(GL2.GL_SMOOTH);

        float dim[] = { 0.5F, 0.5F, 0.5F, 1 };
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, dim, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, dim, 0);

        float red[] =  { 0.5F, 0, 0, 1};
        float reda[] = { 0.1F, 0, 0, 1};
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, reda, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, red, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, red, 0);

        float gr[] = { 0, 0.5F, 0, 1 };
        float gra[] = { 0, 0.1F, 0, 1 };
        gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_AMBIENT, gra, 0);
        gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, gr, 0);
        gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_SPECULAR, gr, 0);

        float bl[] = {0, 0, 0.5F, 1};
        float bla[] = {0, 0, 0.1F, 1};
        gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_AMBIENT, bla, 0);
        gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_DIFFUSE, bl, 0);
        gl. glLightfv(GL2.GL_LIGHT3, GL2.GL_SPECULAR, bl, 0);
        
        
        gl.glEnable(GL2.GL_TEXTURE_2D);
        try{

           File im = new File("C:\\Users\\Dillan\\Documents\\NetBeansProjects\\CMSC405 Week 5\\Textures\\wall.jpg");
           Texture t = TextureIO.newTexture(im, true);
           texture= t.getTextureObject(gl);
        }catch(IOException e){
           e.printStackTrace();
        }
        
        try{

           File im = new File("C:\\Users\\Dillan\\Documents\\NetBeansProjects\\CMSC405 Week 5\\Textures\\grass.jpg");
           Texture t = TextureIO.newTexture(im, true);
           texture2= t.getTextureObject(gl);
        }catch(IOException e){
           e.printStackTrace();
        }
    }

    /**
     * Called when the size of the GLJPanel changes.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    /**
     * This is called before the GLJPanel is destroyed. 
     */
    public void dispose(GLAutoDrawable drawable) {
    }
    
    public void drawTower(GL2 gl) {
        
        gl.glBindTexture(GL2.GL_TEXTURE_2D, texture);
        gl.glBegin(GL2.GL_QUADS);
        
        // Draws the top of the cube
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3d(0.1,0.4,-0.1);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3d(-0.1,0.4,-0.1);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3d(-0.1,0.4,0.1);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3d(0.1,0.4,0.1);
        
        // Draws the bottom of the cube
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3d(0.1,0,0.1);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3d(-0.1,0,0.1);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3d(-0.1,0,-0.1);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3d(0.1,0,-0.1);
        
        // Draws the front of the cube
        gl.glTexCoord2d(1, 0); gl.glVertex3d(0.1,0.4,0.1);
        gl.glTexCoord2d(1, 1); gl.glVertex3d(-0.1,0.4,0.1);
        gl.glTexCoord2d(0, 1); gl.glVertex3d(-0.1,0,0.1);
        gl.glTexCoord2d(0, 0); gl.glVertex3d(0.1,0,0.1);
        
        // Draws the back of the cube
        gl.glTexCoord2d(1, 0); gl.glVertex3d(0.1,0,-0.1);
        gl.glTexCoord2d(1, 1); gl.glVertex3d(-0.1,0,-0.1);
        gl.glTexCoord2d(0, 1); gl.glVertex3d(-0.1,0.4,-0.1);
        gl.glTexCoord2d(0, 0); gl.glVertex3d(0.1,0.4,-0.1);
        
        // Draws the left side fo the cube
        gl.glTexCoord2d(1, 0); gl.glVertex3d(-0.1,0.4,0.1);
        gl.glTexCoord2d(1, 1); gl.glVertex3d(-0.1,0.4,-0.1);
        gl.glTexCoord2d(0, 1); gl.glVertex3d(-0.1,0,-0.1);
        gl.glTexCoord2d(0, 0); gl.glVertex3d(-0.1,0,0.1);
        
        // Draws the right side of the cube
        gl.glTexCoord2d(1, 0); gl.glVertex3d(0.1,0.4,-0.1);
        gl.glTexCoord2d(1, 1); gl.glVertex3d(0.1,0.4,0.1);
        gl.glTexCoord2d(0, 1); gl.glVertex3d(0.1,0,0.1);
        gl.glTexCoord2d(0, 0); gl.glVertex3d(0.1,0,-0.1);
        
        gl.glEnd();
        gl.glFlush();
        
    }
    
    public void drawGate(GL2 gl) {
        gateLeftSide(gl);
        gl.glTranslated(0.3,0,0);
        gateCenter(gl);
        gl.glTranslated(0.2,0,0);
        gateRightSide(gl);
    }
    
    public void gateLeftSide(GL2 gl) {
        gl.glBegin(GL2.GL_QUADS);
        
        // Color blue
        gl.glColor3d(0,0,1);
        
        // Draws the top of the cube
        gl.glVertex3d(0.2,0.3,-0.1);
        gl.glVertex3d(-0.1,0.3,-0.1);
        gl.glVertex3d(-0.1,0.3,0.1);
        gl.glVertex3d(0.2,0.3,0.1);
        
        // Draws the bottom of the cube
        gl.glVertex3d(0.2,0,0.1);
        gl.glVertex3d(-0.1,0,0.1);
        gl.glVertex3d(-0.1,0,-0.1);
        gl.glVertex3d(0.2,0,-0.1);
        
        // Draws the front of the cube
        gl.glVertex3d(0.2,0.3,0.1);
        gl.glVertex3d(-0.1,0.3,0.1);
        gl.glVertex3d(-0.1,0,0.1);
        gl.glVertex3d(0.2,0,0.1);
        
        // Draws the back of the cube
        gl.glVertex3d(0.2,0,-0.1);
        gl.glVertex3d(-0.1,0,-0.1);
        gl.glVertex3d(-0.1,0.3,-0.1);
        gl.glVertex3d(0.2,0.3,-0.1);
        
        // Draws the left side fo the cube
        gl.glVertex3d(-0.1,0.3,0.1);
        gl.glVertex3d(-0.1,0.3,-0.1);
        gl.glVertex3d(-0.1,0,-0.1);
        gl.glVertex3d(-0.1,0,0.1);
        
        // Draws the right side of the cube
        gl.glVertex3d(0.2,0.3,-0.1);
        gl.glVertex3d(0.2,0.3,0.1);
        gl.glVertex3d(0.2,0,0.1);
        gl.glVertex3d(0.2,0,-0.1);
        
        gl.glEnd();
    }
    
    public void gateCenter(GL2 gl) {
        gl.glBegin(GL2.GL_QUADS);
        
        // Color purp
        gl.glColor3d(1,0,1);
        
        // Draws the top of the cube
        gl.glVertex3d(0.1,0.3,-0.1);
        gl.glVertex3d(-0.1,0.3,-0.1);
        gl.glVertex3d(-0.1,0.3,0.1);
        gl.glVertex3d(0.1,0.3,0.1);
        
        gl.glEnd();
        
        // Draws the front of the cube
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        
        gl.glVertex3d(0,0.25,0.1);
        gl.glVertex3d(-0.1,0.2,0.1);
        gl.glVertex3d(-0.1,0.3,0.1);
        gl.glVertex3d(0.1,0.3,0.1);
        gl.glVertex3d(0.1,0.2,0.1);

        gl.glEnd();
        
        // Draws the back of the cube
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        
        gl.glVertex3d(0,0.25,-0.1);
        gl.glVertex3d(-0.1,0.2,-0.1);
        gl.glVertex3d(-0.1,0.3,-0.1);
        gl.glVertex3d(0.1,0.3,-0.1);
        gl.glVertex3d(0.1,0.2,-0.1);
        
        gl.glEnd();
        
        // Draws the left side fo the cube
        gl.glBegin(GL2.GL_QUADS);
        
        gl.glVertex3d(-0.1,0.3,0.1);
        gl.glVertex3d(-0.1,0.3,-0.1);
        gl.glVertex3d(-0.1,0.2,-0.1);
        gl.glVertex3d(-0.1,0.2,0.1);
        
        // Draws the right side of the cube
        gl.glVertex3d(0.1,0.3,-0.1);
        gl.glVertex3d(0.1,0.3,0.1);
        gl.glVertex3d(0.1,0.2,0.1);
        gl.glVertex3d(0.1,0.2,-0.1);
        
        gl.glColor3d(1,1,1);
        
        // Draws the inside left
        gl.glVertex3d(-0.1,0.2,0.1);
        gl.glVertex3d(-0.1,0.2,-0.1);
        gl.glVertex3d(0,0.25,-0.1);
        gl.glVertex3d(0,0.25,0.1);
        
        // Draws the inside right
        gl.glVertex3d(0.1,0.2,-0.1);
        gl.glVertex3d(0.1,0.2,0.1);
        gl.glVertex3d(0,0.25,0.1);
        gl.glVertex3d(0,0.25,-0.1);
        
        gl.glEnd();
    }
    
    public void gateRightSide(GL2 gl) {
        gl.glBegin(GL2.GL_QUADS);
        
        // Color green
        gl.glColor3d(0,1,0);
        
        // Draws the top of the cube
        gl.glVertex3d(0.2,0.3,-0.1);
        gl.glVertex3d(-0.1,0.3,-0.1);
        gl.glVertex3d(-0.1,0.3,0.1);
        gl.glVertex3d(0.2,0.3,0.1);
        
        // Draws the bottom of the cube
        gl.glVertex3d(0.2,0,0.1);
        gl.glVertex3d(-0.1,0,0.1);
        gl.glVertex3d(-0.1,0,-0.1);
        gl.glVertex3d(0.2,0,-0.1);
        
        // Draws the front of the cube
        gl.glVertex3d(0.2,0.3,0.1);
        gl.glVertex3d(-0.1,0.3,0.1);
        gl.glVertex3d(-0.1,0,0.1);
        gl.glVertex3d(0.2,0,0.1);
        
        // Draws the back of the cube
        gl.glVertex3d(0.2,0,-0.1);
        gl.glVertex3d(-0.1,0,-0.1);
        gl.glVertex3d(-0.1,0.3,-0.1);
        gl.glVertex3d(0.2,0.3,-0.1);
        
        // Draws the left side fo the cube
        gl.glVertex3d(-0.1,0.3,0.1);
        gl.glVertex3d(-0.1,0.3,-0.1);
        gl.glVertex3d(-0.1,0,-0.1);
        gl.glVertex3d(-0.1,0,0.1);
        
        // Draws the right side of the cube
        gl.glVertex3d(0.2,0.3,-0.1);
        gl.glVertex3d(0.2,0.3,0.1);
        gl.glVertex3d(0.2,0,0.1);
        gl.glVertex3d(0.2,0,-0.1);
        
        gl.glEnd();
    }




}