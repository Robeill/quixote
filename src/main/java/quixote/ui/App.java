package quixote.ui;

import quixote.core.*;

import io.qt.gui.QAction;
import io.qt.gui.QKeyEvent;
import io.qt.widgets.*;

import java.sql.SQLException;

import io.qt.QtPrimitiveType;
import io.qt.core.QEvent;
import io.qt.core.Qt;

public class App extends QWidget {
    public static App app;
    public static Database db;

    private QWidget mainWindow;
    private QWidget viewArea;
    private QStackedLayout layout; // layout manager for viewArrea
    private Editor editor;
    private Selector selector;
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

        try{
            QApplication.initialize(args);
            QApplication.setCursorFlashTime(0);
            db = new Database(); // This must be preceed new App()
            app = new App();
        }
        catch(SQLException e){
            System.out.println("FATAL: Unable to intialize database: " + e.getMessage());
        }
        catch(Exception e){
            System.out.println("FATAL: Unknow error occurred: " + e.getMessage());
        }
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
        selector = new Selector(viewArea);
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
        mainWindow.dispose();
        QApplication.quit();
    }

    public void openNote(Note note){
        normalMode = false;
        modeChanged.emit("INSERT");
        editor.newBuffer(note);
        switchView();
    }

    private void cleanup(){
        editor.save();
        db.shutdown();
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
                if(layout.currentWidget() == selector && ! editor.isEmpty()) {
                    layout.setCurrentWidget(editor);
                }
                else if(layout.currentWidget() == editor){
                    layout.setCurrentWidget(selector);
                }
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
}
