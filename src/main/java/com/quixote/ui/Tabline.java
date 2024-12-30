package com.quixote.ui;

import io.qt.widgets.*;

final public class Tabline extends QWidget {
    public Tabline(QWidget parent){
        super(parent);
        this.setLayout(new QHBoxLayout());
    }

    public void newTab(QWidget tab){
        this.layout().addWidget(tab);
    }
}
