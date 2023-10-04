package br.com.techsneeker.service;

public class ProfitCalculator {

    private static final double DEFAULT_CREATION_FEE = 0.01;
    private static final double TEN_MILLION_CREATION_FEE = 0.02;
    private static final double HUNDRED_MILLION_CREATION_FEE = 0.025;
    private static final double MILLION_CLAIM_FEE = 0.01;

    public static Long getProfit(long itemPrice, long lowestBin) {
        return (lowestBin - itemPrice) - getExtraFee(itemPrice);
    }

    private static Long getExtraFee(long itemPrice) {
        return getCreationFee(itemPrice) + getClaimFee(itemPrice);
    }

    private static Long getCreationFee(long itemPrice) {
        if (itemPrice >= 10000000 && itemPrice < 100000000) {
            return Math.round(itemPrice * TEN_MILLION_CREATION_FEE);
        }

        if (itemPrice >= 100000000) {
            return Math.round(itemPrice * HUNDRED_MILLION_CREATION_FEE);
        }

        return Math.round(itemPrice * DEFAULT_CREATION_FEE);
    }

    private static Long getClaimFee(long itemPrice) {
        return (itemPrice >= 1000000)
                ? Math.round(itemPrice * MILLION_CLAIM_FEE)
                : itemPrice;
    }

}
