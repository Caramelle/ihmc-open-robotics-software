package us.ihmc.robotics.linearAlgebra;

import org.ejml.data.DenseMatrix64F;
import org.junit.Test;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;

import static org.junit.Assert.assertEquals;

public abstract class DampedNullspaceCalculatorTest extends NullspaceCalculatorTest
{
   public abstract DampedNullspaceCalculator getDampedNullspaceProjectorCalculator();

   @ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testSimpleNullspaceProjectorWithDamping()
   {
      DampedNullspaceCalculator nullspaceCalculator = getDampedNullspaceProjectorCalculator();

      nullspaceCalculator.setPseudoInverseAlpha(0.1);

      DenseMatrix64F jacobian = new DenseMatrix64F(2, 2);
      jacobian.set(0, 0, 1.0);
      jacobian.set(0, 1, 3.0);
      jacobian.set(1, 0, 7.0);
      jacobian.set(1, 1, 9.0);


      DenseMatrix64F nullspaceProjector = new DenseMatrix64F(2, 2);
      /*
      nullspaceCalculator.computeNullspaceProjector(jacobian, nullspaceProjector);

      DenseMatrix64F nullspaceProjectorExpected = new DenseMatrix64F(2, 2);

      nullspaceProjectorExpected.set(0, 0, 0.0061905);
      nullspaceProjectorExpected.set(0, 1,-0.0045392);
      nullspaceProjectorExpected.set(1, 0,-0.0045392);
      nullspaceProjectorExpected.set(1, 1, 0.00343947);

      for (int i = 0; i < nullspaceProjector.getNumElements(); i++)
         assertEquals(nullspaceProjectorExpected.get(i), nullspaceProjector.get(i), 1e-7);
         */


      jacobian = new DenseMatrix64F(3, 4);
      jacobian.set(0, 0, 1.0);
      jacobian.set(0, 1, 3.0);
      jacobian.set(0, 3, 7.0);
      jacobian.set(1, 0, 7.0);
      jacobian.set(1, 2, 9.0);
      jacobian.set(1, 3, 11.0);
      jacobian.set(2, 1, 13.0);
      jacobian.set(2, 2, 15.0);
      jacobian.set(2, 3, 17.0);

      nullspaceProjector = new DenseMatrix64F(4, 4);
      nullspaceCalculator.computeNullspaceProjector(jacobian, nullspaceProjector);

      DenseMatrix64F nullspaceProjectorExpected = new DenseMatrix64F(4, 4);

      nullspaceProjectorExpected.set(0, 0, 0.501058);
      nullspaceProjectorExpected.set(0, 1, 0.423592);
      nullspaceProjectorExpected.set(0, 2,-0.0802936);
      nullspaceProjectorExpected.set(0, 3,-0.253097);

      nullspaceProjectorExpected.set(1, 0, 0.423592);
      nullspaceProjectorExpected.set(1, 1, 0.35831);
      nullspaceProjectorExpected.set(1, 2,-0.0679324);
      nullspaceProjectorExpected.set(1, 3,-0.214036);

      nullspaceProjectorExpected.set(2, 0,-0.0802936);
      nullspaceProjectorExpected.set(2, 1,-0.0679324);
      nullspaceProjectorExpected.set(2, 2, 0.0131833);
      nullspaceProjectorExpected.set(2, 3, 0.0403421);

      nullspaceProjectorExpected.set(3, 0,-0.253097);
      nullspaceProjectorExpected.set(3, 1,-0.214036);
      nullspaceProjectorExpected.set(3, 2, 0.0403421);
      nullspaceProjectorExpected.set(3, 3, 0.128071);

      for (int i = 0; i < nullspaceProjectorExpected.getNumElements(); i++)
         assertEquals(nullspaceProjectorExpected.get(i), nullspaceProjector.get(i), 1e-5);


      jacobian = new DenseMatrix64F(2, 4);
      jacobian.set(0, 0, 1.0);
      jacobian.set(0, 1, 3.0);
      jacobian.set(0, 3, 7.0);
      jacobian.set(1, 0, 7.0);
      jacobian.set(1, 2, 9.0);
      jacobian.set(1, 3, 11.0);

      nullspaceProjector = new DenseMatrix64F(4, 4);
      nullspaceCalculator.computeNullspaceProjector(jacobian, nullspaceProjector);

      nullspaceProjectorExpected = new DenseMatrix64F(4, 4);

      nullspaceProjectorExpected.set(0, 0, 0.746458);
      nullspaceProjectorExpected.set(0, 1, 0.130345);
      nullspaceProjectorExpected.set(0, 2,-0.381845);
      nullspaceProjectorExpected.set(0, 3,-0.162561);

      nullspaceProjectorExpected.set(1, 0, 0.130345);
      nullspaceProjectorExpected.set(1, 1, 0.708734);
      nullspaceProjectorExpected.set(1, 2, 0.292415);
      nullspaceProjectorExpected.set(1, 3,-0.322225);

      nullspaceProjectorExpected.set(2, 0,-0.381845);
      nullspaceProjectorExpected.set(2, 1, 0.292415);
      nullspaceProjectorExpected.set(2, 2, 0.383735);
      nullspaceProjectorExpected.set(2, 3,-0.0709106);

      nullspaceProjectorExpected.set(3, 0,-0.162561);
      nullspaceProjectorExpected.set(3, 1,-0.322225);
      nullspaceProjectorExpected.set(3, 2,-0.0709106);
      nullspaceProjectorExpected.set(3, 3, 0.161473);

      for (int i = 0; i < nullspaceProjectorExpected.getNumElements(); i++)
         assertEquals(nullspaceProjectorExpected.get(i), nullspaceProjector.get(i), 1e-5);
   }

   /*
   @ContinuousIntegrationTest(estimatedDuration = 0.0)
   @Test(timeout = 30000)
   public void testSimpleProjectOntoNullspace()
   {
      NullspaceCalculator nullspaceCalculator = getDampedNullspaceProjectorCalculator();

      DenseMatrix64F jacobian = new DenseMatrix64F(2, 2);
      jacobian.set(0, 0, 1.0);
      jacobian.set(0, 1, 3.0);
      jacobian.set(1, 0, 7.0);
      jacobian.set(1, 1, 9.0);

      DenseMatrix64F vectorToProject = new DenseMatrix64F(1, 2);
      vectorToProject.set(0, 0, 3.5);
      vectorToProject.set(0, 1, 4.5);

      DenseMatrix64F projectedVector = new DenseMatrix64F(1, 2);

      nullspaceCalculator.projectOntoNullspace(vectorToProject, jacobian, projectedVector);

      assertEquals(0.0, projectedVector.get(0, 0), 1e-7);
      assertEquals(0.0, projectedVector.get(0, 1), 1e-7);


      jacobian = new DenseMatrix64F(2, 4);
      jacobian.set(0, 0, 1.0);
      jacobian.set(0, 1, 3.0);
      jacobian.set(0, 3, 7.0);
      jacobian.set(1, 0, 7.0);
      jacobian.set(1, 2, 9.0);
      jacobian.set(1, 3, 11.0);

      DenseMatrix64F matrixToProject = new DenseMatrix64F(2, 4);
      matrixToProject.set(0, 0, 3.5);
      matrixToProject.set(0, 1, 4.5);
      matrixToProject.set(0, 2, 5.5);
      matrixToProject.set(0, 3, 6.5);
      matrixToProject.set(1, 0, 7.5);
      matrixToProject.set(1, 1, 8.5);
      matrixToProject.set(1, 2, 9.5);
      matrixToProject.set(1, 3, 10.5);

      projectedVector = new DenseMatrix64F(2, 4);

      nullspaceCalculator.projectOntoNullspace(matrixToProject, jacobian, projectedVector);

      double epsilon = 1e-5;
      assertEquals(0.0423707, projectedVector.get(0, 0), epsilon);
      assertEquals(3.15904, projectedVector.get(0, 1), epsilon);
      assertEquals(1.62918, projectedVector.get(0, 2), epsilon);
      assertEquals(-1.35993, projectedVector.get(0, 3), epsilon);

      assertEquals(1.37192, projectedVector.get(1, 0), epsilon);
      assertEquals(6.39598, projectedVector.get(1, 1), epsilon);
      assertEquals(2.52277, projectedVector.get(1, 2), epsilon);
      assertEquals(-2.93712, projectedVector.get(1, 3), epsilon);
   }
   */
}
