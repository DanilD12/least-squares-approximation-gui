/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package coursework;

import java.io.FileWriter;
import java.io.IOException;
import javax.swing.table.TableModel;

public class FileSaver {
    
    public static void saveTableData(TableModel model, String filename) throws IOException {
        try (FileWriter fw = new FileWriter(filename)) {
            
            fw.write("X\tY_исходная\tY_аппроксимация\n");
            
            for (int i = 0; i < model.getRowCount(); i++) {
                Object x = model.getValueAt(i, 0);
                Object ySrc = model.getValueAt(i, 1);
                Object yAppr = model.getValueAt(i, 2);
                
                fw.write(x + "\t" + ySrc + "\t" + yAppr + "\n");
            }
        }
    }
}