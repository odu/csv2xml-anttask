# CSV2XML Ant Task w/ Header Row

This is a fork of the CSV2XML [Ant](http://ant.apache.org/) task project located at: https://code.google.com/p/csv2xml-anttask/ under the [Apache License, Version 2](http://www.apache.org/licenses/LICENSE-2.0).

Additions to this project include support for Header Rows in the CSV that better map the CSV content to XML.  The original Ant Task would take CSV such as:

```
Name,Working Title
Matt,Web Developer
```

And convert it into the XML:

```
<csv>
    <row>
        <c1>Name</c1>
        <c2>Working Title</c2>
    </row>
    <row>
        <c1>Matt</c1>
        <c2>Web Developer</c2>
    </row>
</csv>
```

With the new Header row support, the XML output would be, with `root` parameter set to "employees" and `row` set to "employee":

```
<employees>
    <employee>
        <name>Matt</name>
        <working_title>Web Developer</working_title>
    </employee>
</employees>
```

## Usage Example

In ant Ant build file with the built csv2xml-ant.jar available in the same directory or the classpath.  The Ant snippet below would be what would work for the above example.  If the name of the csv were `employees.csv`, the output would be stored in an `employees.xml` file.

```
<taskdef name="csv2xml" classname="ant.CSV2XMLTask">
    <classpath location="csv2xml-ant.jar" />
</taskdef>
<csv2xml dest="." delim="," root="employees" row="employee">
    <fileset dir="./">
        <include name="**/*.csv"/>
    </fileset>
</csv2xml>
```

## Parameters

`dest` String, Required.  Defines the output destination.

`delim` String, Required.  Defines the characte delimiter in the input CSV file.

`root` String, Default: "csv".  Defines the element name of the root node of the XML output.

`row` String, Default: "row".  Defines the element name of each row in the XML output.

`headerRow` Boolean, Default: true.  Will use the values in the first row of the CSV file as the element names for each column in the XML output.

`skipEmpty` Boolean, Default: true.  Will leave off XML nodes for empty columns in the CSV input file.

`lowerCaseTags` Boolean, Default: true.  Will Lower Case the headerRow titles in the element names in the XML output.