package com.company;


import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/** Поток-вычислитель группы ячеек матрицы. */
class MultiplierThread extends Thread
{


    /** Первая (левая) матрица. */
    private final int[][] firstMatrix;
    /** Вторая (правая) матрица. */
    private final int[][] secondMatrix;
    /** Результирующая матрица. */
    private final int[][] resultMatrix;
    /** Начальный индекс. */
    private final int firstIndex;
    /** Конечный индекс. */
    private final int lastIndex;
    /** Число членов суммы при вычислении значения ячейки. */
    private final int sumLength;

    private int countThread;

    CyclicBarrier cyclicBarrier;

    /**
     * @param firstMatrix  Первая (левая) матрица.
     * @param secondMatrix Вторая (правая) матрица.
     * @param resultMatrix Результирующая матрица.
     * @param firstIndex   Начальный индекс (ячейка с этим индексом вычисляется).
     * @param lastIndex    Конечный индекс (ячейка с этим индексом не вычисляется).
     * @param countThread
     * @param cyclicBarrier
     */
    public MultiplierThread(final int[][] firstMatrix,
                            final int[][] secondMatrix,
                            final int[][] resultMatrix,
                            final int firstIndex,
                            final int lastIndex,
                            final int countThread, CyclicBarrier cyclicBarrier)
    {
        this.firstMatrix  = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.resultMatrix = resultMatrix;
        this.firstIndex   = firstIndex;
        this.lastIndex    = lastIndex;
        this.cyclicBarrier=cyclicBarrier;
        sumLength = secondMatrix.length;
        this.countThread = countThread;
    }

    /**Вычисление значения в одной ячейке.
     *
     * @param row Номер строки ячейки.
     * @param col Номер столбца ячейки.
     */
    private void calcValue(final int row, final int col)
    {
        int sum = 0;
        for (int i = 0; i < sumLength; ++i)
            sum += firstMatrix[row][i] * secondMatrix[i][col];
        resultMatrix[row][col] = sum;
    }

    /** Рабочая функция потока. */
    @Override
    public void run()
    {

        try {
            cyclicBarrier.await();//снимаем барьер на 1
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

        System.out.println("Thread " + getName() + " started. Calculating cells from " + firstIndex + " to " + lastIndex + "...");

        final int colCount = secondMatrix[0].length;  // Число столбцов результирующей матрицы.
        for (int index = firstIndex; index < lastIndex; ++index)
            calcValue(index / colCount, index % colCount);


        System.out.println("Thread " + getName() + " finished.");
    }




}
