/*
 * OpenAPI Petstore
 *
 * This spec is mainly for testing Petstore server and contains fake endpoints, models. Please do not use this for any other purpose. Special characters: \" \\
 *
 * API version: 1.0.0
 * Generated by: OpenAPI Generator (https://openapi-generator.tech)
 */

package petstore
import (
	"os"
	"time"
)

type FormatTest struct {
	Integer int32 `json:"integer,omitempty"`
	Int32 int32 `json:"int32,omitempty"`
	Int64 int64 `json:"int64,omitempty"`
	Number float32 `json:"number"`
	Float float32 `json:"float,omitempty"`
	Double float64 `json:"double,omitempty"`
	String string `json:"string,omitempty"`
	Byte string `json:"byte"`
	Binary *os.File `json:"binary,omitempty"`
	Date string `json:"date"`
	DateTime time.Time `json:"dateTime,omitempty"`
	Uuid string `json:"uuid,omitempty"`
	Password string `json:"password"`
	// A string that is a 10 digit number. Can have leading zeros.
	PatternWithDigits string `json:"pattern_with_digits,omitempty"`
	// A string starting with 'image_' (case insensitive) and one to three digits following i.e. Image_01.
	PatternWithDigitsAndDelimiter string `json:"pattern_with_digits_and_delimiter,omitempty"`
}
