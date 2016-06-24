package com.nzion.util;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;


public class CurrencyConverter {

	private static final String[] tensNames = {
			"",
			" Ten",
			" Twenty",
			" Thirty",
			" Forty",
			" Fifty",
			" Sixty",
			" Seventy",
			" Eighty",
			" Ninety"
	};

	private static final String[] numNames = {
			"",
			" One",
			" Two",
			" Three",
			" Four",
			" Five",
			" Six",
			" Seven",
			" Eight",
			" Nine",
			" Ten",
			" Eleven",
			" Twelve",
			" Thirteen",
			" Fourteen",
			" Fifteen",
			" Sixteen",
			" Seventeen",
			" Eighteen",
			" Nineteen"
	};

	private static String cleanNumber(String number) {
		String cleanedNumber = "";
		cleanedNumber = number.replace('.', ' ').replaceAll(" ", "");
		cleanedNumber = cleanedNumber.replace(',', ' ').replaceAll(" ", "");
		if (cleanedNumber.startsWith("0"))
			cleanedNumber = cleanedNumber.replaceFirst("0", "");

		return cleanedNumber;
	} //cleanNumber

	public static String convertNumber(int number) {
		String snumber = Long.toString(number);
		String soFar;
		String mask = "000000000000";
		DecimalFormat df = new DecimalFormat(mask);
		snumber = df.format(number);

		// XXXnnnnnnnnn
		int billions = Integer.parseInt(snumber.substring(0,3));
		// nnnXXXnnnnnn
		int millions  = Integer.parseInt(snumber.substring(3,6));
		// nnnnnnXXXnnn
		int hundredThousands = Integer.parseInt(snumber.substring(6,9));
		// nnnnnnnnnXXX
		int thousands = Integer.parseInt(snumber.substring(9,12));


		String tradBillions;
		switch (billions) {
			case 0:
				tradBillions = "";
				break;
			case 1 :
				tradBillions = convertLessThanOneThousand(billions)
						+ " Billion ";
				break;
			default :
				tradBillions = convertLessThanOneThousand(billions)
						+ " Billion ";
		}
		String result =  tradBillions;

		String tradMillions;
		switch (millions) {
			case 0:
				tradMillions = "";
				break;
			case 1 :
				tradMillions = convertLessThanOneThousand(millions)
						+ " Million ";
				break;
			default :
				tradMillions = convertLessThanOneThousand(millions)
						+ " Million ";
		}
		result =  result + tradMillions;
		String tradHundredThousands;
		switch (hundredThousands) {
			case 0:
				tradHundredThousands = "";
				break;
			case 1 :
				tradHundredThousands = "one Thousand ";
				break;
			default :
				tradHundredThousands = convertLessThanOneThousand(hundredThousands)
						+ " Thousand ";
		}
		result =  result + tradHundredThousands;


		String tradThousand;
		tradThousand = convertLessThanOneThousand(thousands);
		result =  result + tradThousand;
		return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
	}

	private static String convertLessThanOneThousand(int number) {
		String soFar;

		if (number % 100 < 20){
			soFar = numNames[number % 100];
			number /= 100;
		}
		else {
			soFar = numNames[number % 10];
			number /= 10;

			soFar = tensNames[number % 10] + soFar;
			number /= 10;
		}
		if (number == 0) return soFar;
		return numNames[number] + " Hundred" + soFar;
	}
	/**
	 * precision is how many decimal places to convert to words. By default 2 *
	 */
	public static String rupeesInWords(String number, String delimiter, Integer precision) {
		String answer = "CANNOT CONVERT";
		if (delimiter == null) {
			delimiter = ".";
		}
		if (precision == null) {
			precision = number.substring(number.lastIndexOf(".")+1).length();
		}
		int delimiterPos = number.indexOf(delimiter);
		try {
			BigDecimal decimalNumber = new BigDecimal(number).setScale(3, BigDecimal.ROUND_HALF_UP);
			if (decimalNumber.compareTo(BigDecimal.ZERO) < 1) {
				answer = "Zero PHP";
				return answer;
			}
		} catch (Exception e) {

		}
		if (delimiterPos > 0) {
			int left = Integer.parseInt(number.substring(0, delimiterPos));
			int right = Integer.parseInt( number.substring( (delimiterPos + 1), (delimiterPos + 1) + (precision) ) );
			System.out.println(right);
			if (StringUtils.isNotEmpty(convertNumber(right)))
				answer = convertNumber(left) + " PHP and " + convertNumber(right) + " Fils.";
			else
				answer = convertNumber(left) + " PHP.";
		} else {
			answer = convertNumber(Integer.parseInt(number)) + " PHP.";
		}
		return Character.toUpperCase(answer.charAt(0)) + answer.substring(1);
	}

	public static String rupeesInWords(Object number) {
		if (number == null)
			return "";
		return rupeesInWords(number.toString(), ".", 3);
	}


} //class
