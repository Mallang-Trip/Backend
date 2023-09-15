package mallang_trip.backend.domain.dto.destination;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.destination.Destination;

@Builder
@Getter
public class MapRequest {

	Double lonA;
	Double latA;
	Double lonB;
	Double latB;
	Double lonC;
	Double latC;
	Double lonD;
	Double latD;

	public Boolean isInSquare(Destination destination){
		double[][] square = {{lonA, latA}, {lonB, latB}, {lonC, latC}, {lonD, latD}};
		double[] point = {destination.getLon(), destination.getLat()};

		double[] A = square[0];
		double[] B = square[1];
		double[] C = square[2];
		double[] D = square[3];

		double[] AB = vectorSubtraction(B, A);
		double[] BC = vectorSubtraction(C, B);
		double[] CD = vectorSubtraction(D, C);
		double[] DA = vectorSubtraction(A, D);
		double[] AP = vectorSubtraction(point, A);
		double[] BP = vectorSubtraction(point, B);
		double[] CP = vectorSubtraction(point, C);
		double[] DP = vectorSubtraction(point, D);

		double crossProductABAP = crossProduct(AB, AP);
		double crossProductBCBP = crossProduct(BC, BP);
		double crossProductCDCP = crossProduct(CD, CP);
		double crossProductDADP = crossProduct(DA, DP);

		return (crossProductABAP >= 0 && crossProductBCBP >= 0
			&& crossProductCDCP >= 0 && crossProductDADP >= 0);
	}

	public static double[] vectorSubtraction(double[] a, double[] b) {
		double[] result = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i] - b[i];
		}
		return result;
	}

	public static double crossProduct(double[] a, double[] b) {
		return a[0] * b[1] - a[1] * b[0];
	}
}
