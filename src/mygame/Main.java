package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
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

    private Geometry[] fondos = new Geometry[10];
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

    // --- NUEVAS VARIABLES PARA EL SCORE ---
    private int score = 0;
    private float timerScore = 0f;
    private BitmapText textoScore;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        // Carga de los 10 fondos
        fondos[0] = crearFondo("Textures/Backgrounds/fondo1.png", -10f, -5f);
        fondos[1] = crearFondo("Textures/Backgrounds/fondo2.png",  10f, -5f);
        fondos[2] = crearFondo("Textures/Backgrounds/fondo3.png",  30f, -5f);
        fondos[3] = crearFondo("Textures/Backgrounds/fondo4.png",  50f, -5f);
        fondos[4] = crearFondo("Textures/Backgrounds/fondo5.png",  70f, -5f);
        fondos[5] = crearFondo("Textures/Backgrounds/fondo6.png",  90f, -5f);
        fondos[6] = crearFondo("Textures/Backgrounds/fondo7.png",  110f, -5f);
        fondos[7] = crearFondo("Textures/Backgrounds/fondo8.png",  130f, -5f);
        fondos[8] = crearFondo("Textures/Backgrounds/fondo9.png",  150f, -5f);
        fondos[9] = crearFondo("Textures/Backgrounds/fondo10.png", 170f, -5f);

        for (Geometry f : fondos) {
            rootNode.attachChild(f);
        }

        pantallaStart = crearPantalla("Textures/Backgrounds/start.png");
        pantallaGameOver = crearPantalla("Textures/Backgrounds/gameover.png");
        guiNode.attachChild(pantallaStart);

        // --- INTERFAZ DEL SCORE ---
        BitmapFont fuente = assetManager.loadFont("Interface/Fonts/Default.fnt");
        textoScore = new BitmapText(fuente, false);
        textoScore.setSize(30f); // Tamaño de la letra
        textoScore.setText("SCORE: 0");
        // Lo posicionamos arriba a la izquierda de la pantalla
        textoScore.setLocalTranslation(20f, cam.getHeight() - 20f, 0f);
        guiNode.attachChild(textoScore);

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
        
        // TECLA DE PRUEBA DE MUERTE: Presiona la 'K' para probar cómo se congela todo y sale el Game Over
        inputManager.addMapping("SimularMuerte", new KeyTrigger(KeyInput.KEY_K));
        
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("IniciarJuego") && !isPressed && !juegoIniciado && !esGameOver) {
                    juegoIniciado = true;
                    guiNode.detachChild(pantallaStart); 
                }
                
                // --- REINICIO TOTAL DESDE CERO ---
                if (name.equals("ReiniciarJuego") && !isPressed && esGameOver) {
                    guiNode.detachChild(pantallaGameOver);
                    velocidad = 5f; 
                    esGameOver = false; 
                    juegoIniciado = true; 
                    
                    // Reset de Score
                    score = 0;
                    timerScore = 0f;
                    textoScore.setText("SCORE: 0");
                    
                    for (Geometry obs : listaObstaculos) {
                        obs.removeFromParent();
                    }
                    listaObstaculos.clear();
                    timerObstaculo = 0f;
                    tiempoParaSiguiente = 1f;
                    ultimoFueAereo = false;
                }
                
                // Activar la muerte al presionar K (Quítalo cuando tu compañero ponga las colisiones reales)
                if (name.equals("SimularMuerte") && !isPressed && juegoIniciado && !esGameOver) {
                    activarGameOver();
                }
            }
        }, "IniciarJuego", "ReiniciarJuego", "SimularMuerte");
    }

    // --- FUNCIÓN PARA CONGELAR EL JUEGO AL MORIR ---
    public void activarGameOver() {
        esGameOver = true;
        guiNode.attachChild(pantallaGameOver);
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Al retornar aquí, si 'esGameOver' es verdadero, todo se congela automáticamente
        if (!juegoIniciado || esGameOver) {
            return; 
        }

        if (velocidad < velocidadMaxima) {
            velocidad += aceleracion * tpf;
        }

        for (Geometry f : fondos) {
            moverFondo(f, tpf);
        }
        
        // --- LÓGICA DE INCREMENTO DE SCORE ---
        timerScore += tpf;
        if (timerScore >= 0.1f) { // Cada 0.1 segundos aumenta el puntaje
            score += 1;
            textoScore.setText("SCORE: " + score);
            timerScore = 0f;
        }
        
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
            currentX += 200f; 
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
                posicionY = -3.2f; 
                esAereo = false; 
                break;
            case 1: 
                rutaTextura = "Textures/obstacles/tumba.png"; 
                posicionY = -3.03f; 
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