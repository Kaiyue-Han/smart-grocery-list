#!/usr/bin/env python3

# Author: Bernhard Luedtke
# Version: 2023-12-07 1
# Resources used: 
# - https://pandas.pydata.org/docs/reference/api/pandas.DataFrame.to_csv.html
# - https://pandas.pydata.org/docs/reference/api/pandas.read_csv.html
# - https://docs.python.org/3/library/argparse.html
# Make sure to install numpy and pandas before running this python script. 
# (e.g. by `pip install numpy pandas`).
# Python 3 obviously needs to be installed and accessible first.

# Run this script from the command line. You should provide 3 arguments, one of which is optional:
# * input file: filepath to a CSV file. The file shall have ";" as the separator and a "date" column.
# * output file: a filepath for where the resulting CSV file shall be saved (e.g. "output.csv").
# * offset: '-o', or '--offset', followed by an integer that specifies the offset in days. 
#           This argument is optional.
# As an example:
# `python time-shifter.py input.csv output.csv -o 7`
# will take input.csv, apply an offset of 7 to the date in each row, and save it as output.csv.
# A row with date 2023-05-02 in input.csv would become a row with date 2023-05-09 in output.csv.
# If you do NOT supply a value for the "offset" argument, the script will shift the dates 
# by the difference of TODAY'S DATE compared to the MOST RECENT DATE in the input csv file.
# In the end, this means that the dates are shifted such that the most recent date in the csv file 
# matches the current date.
# As an example:
# `python time-shifter.py input.csv output.csv`
# will take input.csv, search for the most recent date it contains, calculate the difference between
# today's date and the most recent date, and then shift all dates by this difference.
# Suppose the most recent date it contains is 2023-09-26. If today's date is 2023-12-07, 
# the difference is 72 days. Thus, all dates will be shifted forward by 72 days.

import pandas as pd
import numpy as np
import argparse
import os
from datetime import date

def add_date_offset(date_string, offset):
    date = pd.to_datetime(date_string)
    modified_date = date + pd.DateOffset(days=offset)
    return modified_date.strftime('%Y-%m-%d')

parser = argparse.ArgumentParser(description='Time-shift dates in a CSV file by applying an offset.')
parser.add_argument('input_file', help='Input CSV file path')
parser.add_argument('output_file', help='Output CSV file path')
parser.add_argument('-o', '--offset', type=int, help="""Offset value in days; 
                    if no value is provided, the difference between the current date 
                    and the most recent date in the input file is used as an offset.""", 
                    required=False)

# Parse command-line arguments
args = parser.parse_args()

input_file = args.input_file
output_file = args.output_file
offset_value = 0

# Read CSV file
data = pd.read_csv(input_file, sep=";", index_col=False)
# Prevent pandas from adding "Unnamed: ..." to the header row
data.drop(data.filter(regex="Unnamed"),axis=1, inplace=True)

# Check if offset value was specified by the user.
# If not specified, use difference between most recent date in file and current date as offset.
if args.offset is None:
    print("time-shifter will shift all dates such that the most recent date will match today's date.")
    # Calculate the difference between today and the most recent date in the csv.
    dt_today = pd.to_datetime(date.today())
    dt_most_recent = pd.to_datetime(data['date'].aggregate(func='max'))
    offset_value = (dt_today - dt_most_recent) / np.timedelta64(1, 'D')
else:
    print("time-shifter will shift all dates by a user-defined amount of days.")
    offset_value = args.offset

print("Dates will be shifted by " + str(offset_value) + " days.")

# Apply offset to the date column
data['date'] = data['date'].apply(lambda x: add_date_offset(x, offset_value))

print("Dates have been shifted, now writing to output file.")

# Save the modified data to a new CSV file,
# including a hack to ensure trailing separators 
# (though your CSV parser should work with and without them anyway)
data.to_csv(output_file, index=False, sep=";", lineterminator=";" + os.linesep)