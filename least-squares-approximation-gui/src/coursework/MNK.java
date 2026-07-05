/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package coursework;

import java.util.function.DoubleUnaryOperator;

public class MNK {

    // ------------------------------------------------------------
    //  КВАДРАТИЧНАЯ АППРОКСИМАЦИЯ: y = a2*x^2 + a1*x + a0
    //  Нормальная система (как в методичке):
    //    x0*a0 + x1*a1 + x2*a2 = y1
    //    x1*a0 + x2*a1 + x3*a2 = xy
    //    x2*a0 + x3*a1 + x4*a2 = x2y
    // ------------------------------------------------------------

    // Суммы (x0, x1, x2, x3, x4, y1, xy, x2y)
    private double x0, x1, x2, x3, x4, y1, xy, x2y;

    private final double[] a = new double[3];
    private final double[] B = new double[3];
    private final double[] D = new double[3];
    private final double[][] A = new double[3][3];

    public static String getApproxFuncStr() {
        return "a2*x^2 + a1*x + a0";
    }

    private void init(double xMin, double xMax, int n, DoubleUnaryOperator func) {
        double dx = (xMax - xMin) / (n - 1);

        x0 = n;
        x1 = x2 = x3 = x4 = y1 = xy = x2y = 0.0;

        for (int i = 0; i < n; i++) {
            double x = xMin + dx * i;
            double y = func.applyAsDouble(x);

            x1 += x;
            x2 += x * x;
            x3 += x * x * x;
            x4 += x * x * x * x;

            y1 += y;
            xy += x * y;
            x2y += (x * x) * y;
        }
    }

    private void init(double[] x, double[] y, int n) {
        x0 = n;
        x1 = x2 = x3 = x4 = y1 = xy = x2y = 0.0;

        for (int i = 0; i < n; i++) {
            double xi = x[i];
            double yi = y[i];

            x1 += xi;
            x2 += xi * xi;
            x3 += xi * xi * xi;
            x4 += xi * xi * xi * xi;

            y1 += yi;
            xy += xi * yi;
            x2y += (xi * xi) * yi;
        }
    }

    private void getA() {
        A[0][0] = x0;  A[0][1] = x1;  A[0][2] = x2;
        A[1][0] = x1;  A[1][1] = x2;  A[1][2] = x3;
        A[2][0] = x2;  A[2][1] = x3;  A[2][2] = x4;

        B[0] = y1;
        B[1] = xy;
        B[2] = x2y;
    }

    private void getA(int k) {
        getA();
        switch (k) {
            case 0:
                for (int i = 0; i < 3; i++) A[i][0] = B[i];
                break;
            case 1:
                for (int i = 0; i < 3; i++) A[i][1] = B[i];
                break;
            case 2:
                for (int i = 0; i < 3; i++) A[i][2] = B[i];
                break;
            default:
        }
    }

    private double det3(double[][] A) {
        double D =
                A[0][0] * (A[1][1] * A[2][2] - A[1][2] * A[2][1])
              - A[0][1] * (A[1][0] * A[2][2] - A[1][2] * A[2][0])
              + A[0][2] * (A[1][0] * A[2][1] - A[1][1] * A[2][0]);
        return D;
    }

    public double[] mnk(double[] x, double[] y, int n) {
        init(x, y, n);
        getA();

        double D3 = det3(A);
        if (Math.abs(D3) < 1e-12) return new double[]{0, 0, 0};

        for (int k = 0; k < 3; k++) {
            getA(k);
            D[k] = det3(A);
            a[k] = D[k] / D3;
        }
        return a.clone();
    }

    // Перегрузка: mnk(xMin, xMax, n, func)
    public double[] mnk(double xMin, double xMax, int n, DoubleUnaryOperator func) {
        init(xMin, xMax, n, func);
        getA();

        double D3 = det3(A);
        if (Math.abs(D3) < 1e-12) return new double[]{0, 0, 0};

        for (int k = 0; k < 3; k++) {
            getA(k);
            D[k] = det3(A);
            a[k] = D[k] / D3;
        }
        return a.clone();
    }

    public static double r2(double[] x, double[] y, int n, double[] coefA012) {
        double ycr = 0.0;
        for (int i = 0; i < n; i++) ycr += y[i];
        ycr /= n;

        double ssr = 0.0, sst = 0.0;
        for (int i = 0; i < n; i++) {
            double ya = coefA012[0] + coefA012[1] * x[i] + coefA012[2] * x[i] * x[i];
            ssr += (y[i] - ya) * (y[i] - ya);
            sst += (y[i] - ycr) * (y[i] - ycr);
        }
        if (Math.abs(sst) < 1e-12) return 0.0;
        return 1.0 - ssr / sst;
    }

    public static double[] solveQuadratic(double[] x, double[] y, int n) {
        return new MNK().mnk(x, y, n);
    }

    public static double[] solveExponential(double[] x, double[] y, int n) {
        // ln(y) = ln(a) + b*x
        double sx = 0, sx2 = 0, sY = 0, sxY = 0;
        int cnt = 0;

        for (int i = 0; i < n; i++) {
            if (y[i] <= 0) continue;
            double Yi = Math.log(y[i]);
            sx += x[i];
            sx2 += x[i] * x[i];
            sY += Yi;
            sxY += x[i] * Yi;
            cnt++;
        }

        double D = cnt * sx2 - sx * sx;
        if (Math.abs(D) < 1e-12) return new double[]{0, 0};

        double A = (sY * sx2 - sx * sxY) / D;     // A = ln(a)
        double B = (cnt * sxY - sx * sY) / D;     // B = b

        return new double[]{Math.exp(A), B};      // a, b
    }

    public static double[] solvePower(double[] x, double[] y, int n) {
        // ln(y) = ln(a) + b*ln(x)
        double sX = 0, sX2 = 0, sY = 0, sXY = 0;
        int cnt = 0;

        for (int i = 0; i < n; i++) {
            if (x[i] <= 0 || y[i] <= 0) continue;
            double Xi = Math.log(x[i]);
            double Yi = Math.log(y[i]);

            sX += Xi;
            sX2 += Xi * Xi;
            sY += Yi;
            sXY += Xi * Yi;
            cnt++;
        }

        double D = cnt * sX2 - sX * sX;
        if (Math.abs(D) < 1e-12) return new double[]{0, 0};

        double A = (sY * sX2 - sX * sXY) / D;  
        double B = (cnt * sXY - sX * sY) / D;   

        return new double[]{Math.exp(A), B};  
    }

    public static double calcApprox(double x, double[] coefs, int methodType) {
        if (coefs == null || coefs.length == 0) return 0;
        switch (methodType) {
            case 0: // Квадратичная: a0 + a1*x + a2*x^2
                return coefs[0] + coefs[1] * x + coefs[2] * x * x;
            case 1: // Экспонента: a*e^(b*x)
                return coefs[0] * Math.exp(coefs[1] * x);
            case 2: // Степенная: a*x^b
                return coefs[0] * Math.pow(x, coefs[1]);
            default:
                return 0;
        }
    }
}
