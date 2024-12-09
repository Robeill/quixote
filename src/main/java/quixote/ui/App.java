package quixote.ui;

import quixote.core.*;

import io.qt.gui.QAction;
import io.qt.gui.QKeyEvent;
import io.qt.widgets.*;
import io.qt.QtPrimitiveType;
import io.qt.core.QEvent;
import io.qt.core.Qt;
import io.qt.core.QObject;

public class App extends QWidget {
    public static App app;
    public static Database db;

    private QWidget mainWindow;
    private QWidget viewArea;
    private QStackedLayout layout; // layout manager for viewArrea
    private Editor editor;
    private Statusline statusline;
    private boolean normalMode = true;

    // signals
    public final Signal1<@QtPrimitiveType Integer> viewChanged = new Signal1<>();
    public final Signal1<String> modeChanged = new Signal1<>();

    private static final int key_esc = Qt.Key.Key_Escape.value();
    private static final int key_i = Qt.Key.Key_I.value();
    private static final int key_tab = Qt.Key.Key_Tab.value();

    public static void initialize(String[] args){
        if(app != null)
            return;

        QApplication.initialize(args);
        QApplication.setCursorFlashTime(0);
        app = new App();
        db = new Database();
    }

    // constructors
    private App(){
        mainWindow = new QWidget();
        mainWindow.setWindowTitle("Quixote");

        var quitAction = new QAction();
        quitAction.setShortcut("CTRL+Q");
        quitAction.triggered.connect(this::stop);
        mainWindow.addAction(quitAction);

        viewArea = new QWidget();
        layout = new QStackedLayout();
        viewArea.setLayout(layout);

        // connect slot
        viewChanged.connect(layout::setCurrentIndex);
        new Selector(viewArea);
        editor = new Editor(viewArea);
        editor.editorEmpty.connect(this::switchView);

        mainWindow.setLayout(new QVBoxLayout());
        mainWindow.layout().addWidget(viewArea);
        statusline = new Statusline(mainWindow, this);
    }

    public void start() {
        mainWindow.show();
        QApplication.exec();
    }

    public void stop() {
        mainWindow.hide();
        cleanup();
        QApplication.quit();
    }

    public void openNote(Note note){
        normalMode = false;
        modeChanged.emit("INSERT");
        editor.newBuffer(note);
        switchView();
    }

    private void cleanup(){
        mainWindow.dispose();
    }

    private void switchView(){
        viewChanged.emit((layout.currentIndex()+1)%2);
    }

    @Override
    public boolean event(QEvent e){
         if(e.type() == QEvent.Type.KeyPress){
            int key = ((QKeyEvent)e).key();
            if(key == key_esc){
                if(!normalMode){
                    normalMode = true;
                    modeChanged.emit("NORMAL");
                    return true;
                }
            }
            else if(key == key_tab){
                // toggle selector/editor
                viewChanged.emit((layout.currentIndex()+1)%2);
                return true;
            }
            else if(key == key_i){
                // change to insert mode
                normalMode = false;
                modeChanged.emit("INSERT");
                return true;
            }
            // FIXME -- Perhaps N is not the right shortcut?
            else if(key == Qt.Key.Key_N.value()){
                editor.showNext();
                return true;
            }
        }

        return super.event(e);
    }

    @Override
    public boolean eventFilter(QObject obj, QEvent event){
        if(event.type() != QEvent.Type.KeyPress)
            return false;

        int key = ((QKeyEvent)event).key();

        if(!normalMode){
            if(key == key_esc){
                // change to normal mode;
                normalMode = true;
                modeChanged.emit("NORMAL");
                return true;
            }
            else {
                return false;
            }
        }

        if(key == key_i){
            // change to insert mode
            normalMode = false;
            modeChanged.emit("INSERT");
        }
        else if(key == key_tab){
            // toggle selector/editor
            switchView();
        }
        else if(key == Qt.Key.Key_N.value()){
            editor.showNext();
        }

        return true;
    }
}