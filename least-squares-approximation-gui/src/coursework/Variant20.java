/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package coursework;

public class Variant20 {
    
    public static double f(double x, double a, double b, double c) {
        return Math.cos(a * x) + c * Math.pow(x, b * x);
    }

    public static String getFuncName() {
        return "cos(ax) + c * x^(bx)";
    }
}