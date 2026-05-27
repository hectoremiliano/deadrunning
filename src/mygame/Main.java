package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import java.util.ArrayList;

public class Main extends SimpleApplication {

    private Geometry fondo1, fondo2, fondo3;
    private Geometry pantallaStart, pantallaGameOver;

    private float velocidad = 5f;          
    private float velocidadMaxima = 25f;   
    private float aceleracion = 0.5f;      
    
    private boolean juegoIniciado = false; 
    private boolean esGameOver = false; 

    private ArrayList<Geometry> listaObstaculos = new ArrayList<>();
    private float timerObstaculo = 0f;
    private float tiempoParaSiguiente = 1f; 
    
    private boolean ultimoFueAereo = false;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        fondo1 = crearFondo("Textures/Backgrounds/fondo1.png", -10, -5f);
        fondo2 = crearFondo("Textures/Backgrounds/fondo2.png", 10, -5f);
        fondo3 = crearFondo("Textures/Backgrounds/fondo3.png", 30, -5f);

        rootNode.attachChild(fondo1);
        rootNode.attachChild(fondo2);
        rootNode.attachChild(fondo3);

        pantallaStart = crearPantalla("Textures/Backgrounds/start.png");
        pantallaGameOver = crearPantalla("Textures/Backgrounds/gameover.png");
        guiNode.attachChild(pantallaStart);

        configurarTeclas();
    }

    private Geometry crearFondo(String ruta, float posicionX, float posicionY) {
        Quad quad = new Quad(20, 10);
        Geometry geo = new Geometry("Fondo", quad);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture(ruta);
        mat.setTexture("ColorMap", tex);
        geo.setMaterial(mat);
        geo.setLocalTranslation(posicionX, posicionY, -5f);
        return geo;
    }

    private Geometry crearPantalla(String ruta) {
        Quad quad = new Quad(cam.getWidth(), cam.getHeight());
        Geometry geo = new Geometry("PantallaUI", quad);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture(ruta);
        mat.setTexture("ColorMap", tex);
        geo.setMaterial(mat);
        geo.setLocalTranslation(0, 0, 0); 
        return geo;
    }

    private void configurarTeclas() {
        inputManager.addMapping("IniciarJuego", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("ReiniciarJuego", new KeyTrigger(KeyInput.KEY_RETURN)); 
        
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("IniciarJuego") && !isPressed && !juegoIniciado && !esGameOver) {
                    juegoIniciado = true;
                    guiNode.detachChild(pantallaStart); 
                }
                
                if (name.equals("ReiniciarJuego") && !isPressed && esGameOver) {
                    guiNode.detachChild(pantallaGameOver);
                    velocidad = 5f; 
                    esGameOver = false; 
                    juegoIniciado = true; 
                    
                    for (Geometry obs : listaObstaculos) {
                        obs.removeFromParent();
                    }
                    listaObstaculos.clear();
                    timerObstaculo = 0f;
                    tiempoParaSiguiente = 1f;
                    ultimoFueAereo = false;
                }
            }
        }, "IniciarJuego", "ReiniciarJuego");
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!juegoIniciado || esGameOver) {
            return; 
        }

        if (velocidad < velocidadMaxima) {
            velocidad += aceleracion * tpf;
        }

        moverFondo(fondo1, tpf);
        moverFondo(fondo2, tpf);
        moverFondo(fondo3, tpf);
        
        timerObstaculo += tpf;
        if (timerObstaculo >= tiempoParaSiguiente) {
            generarObstaculoRandom();
            timerObstaculo = 0f;
        }
        
        moverObstaculos(tpf);
    }

    private void moverFondo(Geometry fondo, float tpf) {
        float currentX = fondo.getLocalTranslation().x;
        currentX -= velocidad * tpf;
        if (currentX <= -30f) {
            currentX += 60f;
        }
        fondo.setLocalTranslation(currentX, fondo.getLocalTranslation().y, -5f);
    }

    private void generarObstaculoRandom() {
        int tipo;
        
        
        if (ultimoFueAereo) {
            tipo = (int)(Math.random() * 2);
        } else {
            tipo = (int)(Math.random() * 4); 
        }

        String rutaTextura = "";
        float posicionY = 0f;
        boolean esAereo = false; 

        switch (tipo) {
            case 0: 
                rutaTextura = "Textures/obstacles/hole.png"; 
                posicionY = -4.5f; 
                esAereo = false; 
                break;
            case 1: 
                rutaTextura = "Textures/obstacles/tumba.png"; 
                posicionY = -4.0f; 
                esAereo = false; 
                break;
            case 2: 
                rutaTextura = "Textures/obstacles/lanza.png"; 
                posicionY = -1.0f; 
                esAereo = true;  
                break;
            case 3: 
                rutaTextura = "Textures/obstacles/pala.png"; 
                posicionY = -0.5f; 
                esAereo = true;  
                break;
        }

       
        if (!esAereo) {
            
            tiempoParaSiguiente = 1.8f + (float)(Math.random() * 1.0f);
        } else {
            tiempoParaSiguiente = 1.0f + (float)(Math.random() * 0.8f);
        }
        
        ultimoFueAereo = esAereo;

        Quad quad = new Quad(2f, 2f);
        Geometry obstaculo = new Geometry("Obstaculo", quad);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture(rutaTextura);
        mat.setTexture("ColorMap", tex);
        
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.setFloat("AlphaDiscardThreshold", 0.1f);
        obstaculo.setMaterial(mat);
        obstaculo.setQueueBucket(RenderQueue.Bucket.Transparent); 

        obstaculo.setLocalTranslation(20f, posicionY, -4f);
        obstaculo.setUserData("esAereo", esAereo);

        rootNode.attachChild(obstaculo);
        listaObstaculos.add(obstaculo);
    }

    private void moverObstaculos(float tpf) {
        for (int i = listaObstaculos.size() - 1; i >= 0; i--) {
            Geometry obs = listaObstaculos.get(i);
            
            boolean esAereo = obs.getUserData("esAereo");
            
            if (esAereo) {
                obs.move(-(velocidad * 1.5f) * tpf, 0, 0);
            } else {
                obs.move(-velocidad * tpf, 0, 0);
            }

            if (obs.getLocalTranslation().x < -25f) {
                obs.removeFromParent(); 
                listaObstaculos.remove(i); 
            }
        }
    }
}