package us.ihmc.commonWalkingControlModules.controlModules.nativeOptimization;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import us.ihmc.utilities.math.MatrixTools;

public class CVXWithCylinderNativeInput
{
   private final double[] A;
   private final double[] b;
   private final double[] C;
   private final double[] Js;
   private final double[] ps;
   private final double[] Ws;
   private final double[] Lambda;
   private final double[] Qrho;
   private final double[] Qphi;
   private final double[] c;
   private final double[] rhoMin;
   private final double[] phiMin;
   private final double[] phiMax;
   private double wRho;

   // conversion helpers
   private final DenseMatrix64F AMatrix;
   private final DenseMatrix64F bMatrix;
   private final DenseMatrix64F CMatrix;
   private final DenseMatrix64F JsMatrix;
   private final DenseMatrix64F psMatrix;
   private final DenseMatrix64F WsMatrix;
   private final DenseMatrix64F LambdaMatrix;
   private final DenseMatrix64F QrhoMatrix;
   private final DenseMatrix64F QphiMatrix;
   private final DenseMatrix64F cMatrix;
   private final DenseMatrix64F rhoMinMatrix;
   private final DenseMatrix64F phiMinMatrix;
   private final DenseMatrix64F phiMaxMatrix;
   private double wPhi;

   public CVXWithCylinderNativeInput()
   {
      int rhoSize = CVXWithCylinderNative.rhoSize;
      int phiSize = CVXWithCylinderNative.phiSize;
      int wrenchLength = CVXWithCylinderNative.wrenchLength;
      int nDoF = CVXWithCylinderNative.nDoF;

      AMatrix = new DenseMatrix64F(wrenchLength, nDoF);
      bMatrix = new DenseMatrix64F(wrenchLength, 1);
      CMatrix = new DenseMatrix64F(wrenchLength, wrenchLength);
      JsMatrix = new DenseMatrix64F(nDoF, nDoF);
      psMatrix = new DenseMatrix64F(nDoF, 1);
      WsMatrix = new DenseMatrix64F(nDoF, nDoF);
      LambdaMatrix = new DenseMatrix64F(nDoF, nDoF);
      QrhoMatrix = new DenseMatrix64F(wrenchLength, rhoSize);
      QphiMatrix = new DenseMatrix64F(wrenchLength, phiSize);
      cMatrix = new DenseMatrix64F(wrenchLength, 1);
      rhoMinMatrix = new DenseMatrix64F(rhoSize, 1);
      phiMinMatrix = new DenseMatrix64F(phiSize, 1);
      phiMaxMatrix = new DenseMatrix64F(phiSize, 1);

      A = new double[AMatrix.getNumElements()];
      b = new double[bMatrix.getNumElements()];
      C = new double[CMatrix.getNumRows()];    // diagonal
      Js = new double[JsMatrix.getNumElements()];
      ps = new double[psMatrix.getNumElements()];
      Ws = new double[WsMatrix.getNumRows()];    // diagonal
      Lambda = new double[LambdaMatrix.getNumRows()];    // diagonal
      Qrho = new double[QrhoMatrix.getNumElements()];
      Qphi = new double[QphiMatrix.getNumElements()];
      c = new double[cMatrix.getNumElements()];
      rhoMin = new double[rhoMinMatrix.getNumElements()];
      phiMin = new double[phiMinMatrix.getNumElements()];
      phiMax = new double[phiMaxMatrix.getNumElements()];
   }

   public double[] getA()
   {
      return A;
   }

   public double[] getB()
   {
      return b;
   }

   public double[] getC()
   {
      return C;
   }

   public double[] getJs()
   {
      return Js;
   }

   public double[] getPs()
   {
      return ps;
   }

   public double[] getWs()
   {
      return Ws;
   }

   public double[] getLambda()
   {
      return Lambda;
   }

   public double[] getQrho()
   {
      return Qrho;
   }
   
   public double[] getQphi()
   {
      return Qphi;
   }

   public double[] getc()
   {
      return c;
   }

   public double[] getRhoMin()
   {
      return rhoMin;
   }
   
   public double[] getPhiMin()
   {
      return phiMin;
   }
   
   public double[] getPhiMax()
   {
      return phiMax;
   }

   public double getwRho()
   {
      return wRho;
   }

   public double getwPhi()
   {
      return wPhi;
   }

   public void setCentroidalMomentumMatrix(DenseMatrix64F A)
   {
      CommonOps.insert(A, this.AMatrix, 0, 0);
      MatrixTools.denseMatrixToArrayColumnMajor(this.AMatrix, this.A);
   }

   public void setMomentumDotEquationRightHandSide(DenseMatrix64F b)
   {
      CommonOps.insert(b, this.bMatrix, 0, 0);
      MatrixTools.denseMatrixToArrayColumnMajor(this.bMatrix, this.b);
   }

   public void setMomentumDotWeight(DenseMatrix64F C)
   {
      // diagonal
      CommonOps.insert(C, this.CMatrix, 0, 0);
      MatrixTools.extractDiagonal(this.CMatrix, this.C);
   }

   public void setSecondaryConstraintJacobian(DenseMatrix64F Js)
   {
      CommonOps.insert(Js, this.JsMatrix, 0, 0);
      MatrixTools.denseMatrixToArrayColumnMajor(this.JsMatrix, this.Js);
   }

   public void setSecondaryConstraintRightHandSide(DenseMatrix64F ps)
   {
      CommonOps.insert(ps, this.psMatrix, 0, 0);
      MatrixTools.denseMatrixToArrayColumnMajor(this.psMatrix, this.ps);
   }

   public void setSecondaryConstraintWeight(DenseMatrix64F Ws)
   {
      // diagonal
      CommonOps.insert(Ws, this.WsMatrix, 0, 0);
      MatrixTools.extractDiagonal(this.WsMatrix, this.Ws);
   }

   public void setJointAccelerationRegularization(DenseMatrix64F Lambda)
   {
      // diagonal
      CommonOps.insert(Lambda, this.LambdaMatrix, 0, 0);
      MatrixTools.extractDiagonal(this.LambdaMatrix, this.Lambda);
   }

   public void setContactPointWrenchMatrix(DenseMatrix64F Qrho)
   {
      CommonOps.insert(Qrho, this.QrhoMatrix, 0, 0);
      MatrixTools.denseMatrixToArrayColumnMajor(this.QrhoMatrix, this.Qrho);
   }
   
   public void setContactPointWrenchMatrixForBoundedCylinderVariables(DenseMatrix64F Qphi)
   {
      CommonOps.insert(Qphi, this.QphiMatrix, 0, 0);
      MatrixTools.denseMatrixToArrayColumnMajor(this.QphiMatrix, this.Qphi);
   }

   public void setWrenchEquationRightHandSide(DenseMatrix64F c)
   {
      CommonOps.insert(c, this.cMatrix, 0, 0);
      MatrixTools.denseMatrixToArrayColumnMajor(this.cMatrix, this.c);
   }

   public void setRhoMin(DenseMatrix64F rhoMin)
   {
      CommonOps.insert(rhoMin, this.rhoMinMatrix, 0, 0);
      MatrixTools.denseMatrixToArrayColumnMajor(this.rhoMinMatrix, this.rhoMin);
   }
   
   public void setPhiMin(DenseMatrix64F phiMin)
   {
      CommonOps.insert(phiMin, this.phiMinMatrix, 0, 0);
      MatrixTools.denseMatrixToArrayColumnMajor(this.phiMinMatrix, this.phiMin);
   }
   
   public void setPhiMax(DenseMatrix64F phiMax)
   {
      CommonOps.insert(phiMax, this.phiMaxMatrix, 0, 0);
      MatrixTools.denseMatrixToArrayColumnMajor(this.phiMaxMatrix, this.phiMax);
   }

   public void setGroundReactionForceRegularization(double wRho)
   {
      this.wRho = wRho;
   }

   public void setPhiRegularization(double wPhi)
   {
      this.wPhi = wPhi;
   }
}

