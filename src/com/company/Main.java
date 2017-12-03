package com.company;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class Main {



   static long  start,end;
    /** Заполнение матрицы случайными числами.
     *
     * @param matrix Заполняемая матрица.
     */
    private static void randomMatrix(final int[][] matrix)
    {
        final Random random = new Random();  // Генератор случайных чисел.

        for (int row = 0; row < matrix.length; ++row)           // Цикл по строкам матрицы.
            for (int col = 0; col < matrix[row].length; ++col)  // Цикл по столбцам матрицы.
                matrix[row][col] = random.nextInt(100);         // Случайное число от 0 до 100.
    }

    //

    /** Вывод матрицы в файл.
     * Производится выравнивание значений для лучшего восприятия.
     *
     * @param fileWriter Объект, представляющий собой файл для записи.
     * @param matrix Выводимая матрица.
     * @throws IOException
     */
    private static void printMatrix(final FileWriter fileWriter,
                                    final int[][] matrix) throws IOException
    {
        boolean hasNegative = false;  // Признак наличия в матрице отрицательных чисел.
        int     maxValue    = 0;      // Максимальное по модулю число в матрице.

        // Вычисляем максимальное по модулю число в матрице и проверяем на наличие отрицательных чисел.
        for (final int[] row : matrix) {  // Цикл по строкам матрицы.
            for (final int element : row) {  // Цикл по столбцам матрицы.
                int temp = element;
                if (element < 0) {
                    hasNegative = true;
                    temp = -temp;
                }
                if (temp > maxValue)
                    maxValue = temp;
            }
        }

        // Вычисление длины позиции под число.
        int len = Integer.toString(maxValue).length() + 1;  // Одно знакоместо под разделитель (пробел).
        if (hasNegative)
            ++len;  // Если есть отрицательные, добавляем знакоместо под минус.

        // Построение строки формата.
        final String formatString = "%" + len + "d";

        // Вывод элементов матрицы в файл.
        for (final int[] row : matrix) {  // Цикл по строкам матрицы.
            for (final int element : row)  // Цикл по столбцам матрицы.
                fileWriter.write(String.format(formatString, element));

            fileWriter.write("\n");  // Разделяем строки матрицы переводом строки.
        }
    }

    /**
     * Вывод трёх матриц в файл. Файл будет перезаписан.
     *
     * @param fileName     Имя файла для вывода.
     * @param firstMatrix  Первая матрица.
     * @param secondMatrix Вторая матрица.
     * @param resultMatrix Результирующая матрица.
     */
    private static void printAllMatrix(final String fileName,
                                       final int[][] firstMatrix,
                                       final int[][] secondMatrix,
                                       final int[][] resultMatrix)
    {
        try (final FileWriter fileWriter = new FileWriter(fileName, false)) {
            fileWriter.write("First matrix:\n");
            printMatrix(fileWriter, firstMatrix);

            fileWriter.write("\nSecond matrix:\n");
            printMatrix(fileWriter, secondMatrix);

            fileWriter.write("\nResult matrix:\n");
            printMatrix(fileWriter, resultMatrix);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /** Многопоточное умножение матриц.
     *
     * @param firstMatrix  Первая (левая) матрица.
     * @param secondMatrix Вторая (правая) матрица.
     * @param threadCount Число потоков.
     * @return Результирующая матрица.
     */
    private static int[][] multiplyMatrixMT(final int[][] firstMatrix,
                                            final int[][] secondMatrix,
                                            int threadCount) throws BrokenBarrierException, InterruptedException {


        final int rowCount = firstMatrix.length;             // Число строк результирующей матрицы.
        final int colCount = secondMatrix[0].length;         // Число столбцов результирующей матрицы.
        final int[][] result = new int[rowCount][colCount];  // Результирующая матрица.

        final int cellsForThread = (rowCount * colCount) / threadCount;  // Число вычисляемых ячеек на поток.
        int firstIndex = 0;  // Индекс первой вычисляемой ячейки.
        final MultiplierThread[] multiplierThreads = new MultiplierThread[threadCount];  // Массив потоков.
        CyclicBarrier cyclicBarrier =new CyclicBarrier(threadCount+1);
        // Создание и запуск потоков.
        for (int threadIndex = threadCount - 1; threadIndex >= 0; --threadIndex) {
            int lastIndex = firstIndex + cellsForThread;  // Индекс последней вычисляемой ячейки.
            if (threadIndex == 0) {
                /* Один из потоков должен будет вычислить не только свой блок ячеек,
                   но и остаток, если число ячеек не делится нацело на число потоков. */
                lastIndex = rowCount * colCount;
            }
            multiplierThreads[threadIndex] = new MultiplierThread(firstMatrix, secondMatrix, result,
                    firstIndex, lastIndex,threadCount,cyclicBarrier);

            firstIndex = lastIndex;
        }

        for (int threadIndex = threadCount - 1; threadIndex >= 0; --threadIndex) {

            multiplierThreads[threadIndex].start();

        }
        start = System.currentTimeMillis();
        cyclicBarrier.await();

        // Ожидание завершения потоков.
        try {
            for (final MultiplierThread multiplierThread : multiplierThreads)
                multiplierThread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    /** Число строк первой матрицы. */
    final static int FIRST_MATRIX_ROWS  = 100;
    /** Число столбцов первой матрицы. */
    final static int FIRST_MATRIX_COLS  = 100;
    /** Число строк второй матрицы (должно совпадать с числом столбцов первой матрицы). */
    final static int SECOND_MATRIX_ROWS = FIRST_MATRIX_COLS;
    /** Число столбцов второй матрицы. */
    final static int SECOND_MATRIX_COLS = 100;

    public static void main(String[] args) throws IOException, BrokenBarrierException, InterruptedException {
        final int[][] firstMatrix  = new int[FIRST_MATRIX_ROWS][FIRST_MATRIX_COLS];    // Первая (левая) матрица.
        final int[][] secondMatrix = new int[SECOND_MATRIX_ROWS][SECOND_MATRIX_COLS];  // Вторая (правая) матрица.
        PrintWriter fileWriter =new PrintWriter(
                new FileWriter("D:\\IdeaProjects\\Тяп_тест_лаба1\\output.txt",false));
        randomMatrix(firstMatrix);
        randomMatrix(secondMatrix);
        int[][] resultMatrixMT;
        int kolPotokov=1;
        XYSeries s1 = new XYSeries(5);
        resultMatrixMT = multiplyMatrixMT(firstMatrix, secondMatrix, kolPotokov);
        end = System.currentTimeMillis();
        long traceTime[]=new long[101];
        traceTime[0]=end-start;
        try{
            fileWriter.println("Количество потоков: "+kolPotokov+" время выполнения :"+traceTime[0]);
        }finally {

        }
        s1.add(kolPotokov, (double) traceTime[0]/1000);
        kolPotokov++;
        for (int i = 1; i<100; i++) {


            resultMatrixMT = multiplyMatrixMT(firstMatrix, secondMatrix, kolPotokov);

            end=System.currentTimeMillis();
            traceTime[i]=end-start;
            try{
                fileWriter.println("Количество потоков: "+kolPotokov+" время выполнения :"+traceTime[i]);
                s1.add(kolPotokov, (double) traceTime[i]/1000);
            }finally {

            }
            kolPotokov+=4;
        }

        fileWriter.close();
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1);

        JFreeChart chart = createChart(dataset);
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(true);

        JFrame frame = new JFrame("График");
        frame.add(panel);
        frame.setSize(1100,700);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);



        printAllMatrix("Matrix.txt", firstMatrix, secondMatrix, resultMatrixMT);
    }

    private static JFreeChart createChart(XYDataset dataset)
    {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Зависимость времени от количества потоков",
                "Количество потоков",
                "Время с.", dataset);
        return chart;
    }

}
