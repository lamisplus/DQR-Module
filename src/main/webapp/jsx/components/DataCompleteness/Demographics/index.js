import React, { useEffect, useState } from "react";
import axios from "axios";
import MaterialTable from 'material-table';
import { token, url as baseUrl } from "../../../../api";
import {  Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";
import {  Button, } from "semantic-ui-react";

import { forwardRef } from 'react';
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";
import AddBox from '@material-ui/icons/AddBox';
import ArrowUpward from '@material-ui/icons/ArrowUpward';
import Check from '@material-ui/icons/Check';
import ChevronLeft from '@material-ui/icons/ChevronLeft';
import ChevronRight from '@material-ui/icons/ChevronRight';
import Clear from '@material-ui/icons/Clear';
import DeleteOutline from '@material-ui/icons/DeleteOutline';
import Edit from '@material-ui/icons/Edit';
import FilterList from '@material-ui/icons/FilterList';
import FirstPage from '@material-ui/icons/FirstPage';
import LastPage from '@material-ui/icons/LastPage';
import Remove from '@material-ui/icons/Remove';
import SaveAlt from '@material-ui/icons/SaveAlt';
import Search from '@material-ui/icons/Search';
import ViewColumn from '@material-ui/icons/ViewColumn';

const tableIcons = {
  Add: forwardRef((props, ref) => <AddBox {...props} ref={ref} />),
  Check: forwardRef((props, ref) => <Check {...props} ref={ref} />),
  Clear: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Delete: forwardRef((props, ref) => <DeleteOutline {...props} ref={ref} />),
  DetailPanel: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
  Edit: forwardRef((props, ref) => <Edit {...props} ref={ref} />),
  Export: forwardRef((props, ref) => <SaveAlt {...props} ref={ref} />),
  Filter: forwardRef((props, ref) => <FilterList {...props} ref={ref} />),
  FirstPage: forwardRef((props, ref) => <FirstPage {...props} ref={ref} />),
  LastPage: forwardRef((props, ref) => <LastPage {...props} ref={ref} />),
  NextPage: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
  PreviousPage: forwardRef((props, ref) => <ChevronLeft {...props} ref={ref} />),
  ResetSearch: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Search: forwardRef((props, ref) => <Search {...props} ref={ref} />),
  SortArrow: forwardRef((props, ref) => <ArrowUpward {...props} ref={ref} />),
  ThirdStateCheck: forwardRef((props, ref) => <Remove {...props} ref={ref} />),
  ViewColumn: forwardRef((props, ref) => <ViewColumn {...props} ref={ref} />)
  };

const useStyles = makeStyles((theme) => ({
  card: {
    margin: theme.spacing(20),
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  },
  form: {
    width: "100%", // Fix IE 11 issue.
    marginTop: theme.spacing(3),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
  cardBottom: {
    marginBottom: 20,
  },
  Select: {
    height: 45,
    width: 300,
  },
  button: {
    margin: theme.spacing(1),
  },
  root: {
    "& > *": {
      margin: theme.spacing(1),
    },
    "& .card-title": {
      color: "#fff",
      fontWeight: "bold",
    },
    "& .form-control": {
      borderRadius: "0.25rem",
      height: "41px",
    },
    "& .card-header:first-child": {
      borderRadius: "calc(0.25rem - 1px) calc(0.25rem - 1px) 0 0",
    },
    "& .dropdown-toggle::after": {
      display: " block !important",
    },
    "& select": {
      "-webkit-appearance": "listbox !important",
    },
    "& p": {
      color: "red",
    },
    "& label": {
      fontSize: "14px",
      color: "#014d88",
      fontWeight: "bold",
    },
  },
  demo: {
    backgroundColor: theme.palette.background.default,
  },
  inline: {
    display: "inline",
  },
  error: {
    color: "#f85032",
    fontSize: "12.8px",
  },
  success: {
    color: "#4BB543 ",
    fontSize: "11px",
  },
}));

const DemographicsDQA = (props) => {
  const classes = useStyles();
  const [demographics, setDemographic] = useState({});
  const [showPatientDetail, setPatientDetail] = useState(false);
  const [getHeaderInfo, setGetHeaderInfo] = useState("");
  const [demographicsPatientsView, setDemographicsPatientsView] = useState([])

    useEffect(() => {
      loadDemography();
    }, []);

    
  const loadDemography = () => {
    axios
      .get(`${baseUrl}dqr/patient-demo-summary`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setDemographic(response.data);
        console.log(response.data)
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const viewDetail =(headerTitle,patientDemoObj)=>{
  setPatientDetail(true)
  setGetHeaderInfo(headerTitle)
  const patientDemo =patientDemoObj
  axios
        .get(`${baseUrl}dqr/patient-demo?indicator=${patientDemo}`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((response) => {
          setDemographicsPatientsView(response.data);
          //console.log(response.data[0])
        })
        .catch((error) => {
          console.log(error);
        });
  }
  const BackToList=()=> {
    setPatientDetail(false)
  }

  return (
    <>
      <Card className={classes.root}>
        <CardContent>
          <h3>Demographics Variables</h3>
          <div className="col-xl-12 col-lg-12">
          {!showPatientDetail &&(<>
            <Table bordered>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Complete Variables</th>
                  <th>Numerator</th>
                  <th>Denominator</th>
                  <th>Variance</th>
                  <th>Performance</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <th scope="row">1</th>
                  <td>
                    Proportion of all active patients with Date of Birth (DOB)
                  </td>
                  <td>{demographics[0]?.dobNumerator}</td>
                  <td>{demographics[0]?.dobDenominator}</td>
                   <td>{demographics[0]?.dobVariance}</td>
                   <td style={{ backgroundColor: demographics[0]?.dobPerformance  >= 95 ? 'green' : demographics[0]?.dobPerformance >= 90 ? 'yellow' : 'red', color: demographics[0]?.dobPerformance >= 95 ? 'white' : demographics[0]?.dobPerformance>= 90 ? 'black' : 'white',
                   textAlign: 'center', fontWeight: 'bold' }}>
                   {demographics[0]?.dobPerformance} % </td>
                  
                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                   onClick={() => viewDetail("Proportion of all active patients with Date of Birth (DOB)", "patientDemo0" )}
                   >View</Button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">2</th>
                  <td>Proportion of all active patients with Current Age</td>
                  <td>{demographics[0]?.ageNumerator}</td>
                   <td>{demographics[0]?.ageDenominator}</td>
                   <td>{demographics[0]?.ageVariance}</td>
                   <td style={{ backgroundColor: demographics[0]?.agePerformance >= 95 ? 'green' : demographics[0]?.agePerformance >= 90 ? 'yellow' : 'red', color: demographics[0]?.agePerformance >= 95 ? 'white' : demographics[0]?.agePerformance >= 90 ? 'black' : 'white',
                   textAlign: 'center', fontWeight: 'bold' }}>
                    {demographics[0]?.agePerformance} %
                   </td>

                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                   onClick={() => viewDetail("Proportion of all active patients with Current Age", "patientDemo1" )}
                   >View</Button>
                  </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">3</th>
                  <td>
                    Proportion of all active patients with Patient Identifier
                  </td>
                  <td>{demographics[0]?.pidNumerator}</td>
                  <td>{demographics[0]?.pidDenominator}</td>
                  <td>{demographics[0]?.pidVariance}</td>
                  <td style={{ backgroundColor: demographics[0]?.pidPerformance >= 95 ? 'green' : demographics[0]?.pidPerformance >= 90 ? 'yellow' : 'red', color: demographics[0]?.pidPerformance >= 95 ? 'white' : demographics[0]?.pidPerformance >= 90 ? 'black' : 'white',
                   textAlign: 'center', fontWeight: 'bold' }}>
                    {demographics[0]?.pidPerformance} %
                  </td>

                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                   onClick={() => viewDetail("Proportion of all active patients with Patient Identifier", "patientDemo7" )}
                   >View</Button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">4</th>
                  <td>Proportion of all active patients with Sex</td>
                  <td>{demographics[0]?.sexNumerator}</td>
                  <td>{demographics[0]?.sexDenominator}</td>
                  <td>{demographics[0]?.sexVariance}</td>
                  <td style={{ backgroundColor: demographics[0]?.sexPerformance >= 95 ? 'green' : demographics[0]?.sexPerformance >= 90 ? 'yellow' : 'red', color: demographics[0]?.sexPerformance >= 95 ? 'white' : demographics[0]?.sexPerformance >= 90 ? 'black' : 'white',
                   textAlign: 'center', fontWeight: 'bold' }}>
                    {demographics[0]?.sexPerformance} %
                 </td>

                  <td><div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                   onClick={() => viewDetail("Proportion of all active patients with Sex", "patientDemo2" )}
                   >View</Button>
                  </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">5</th>
                  <td>
                    Proportion of all active patients with a documented
                    educational Status
                  </td>
                  <td>{demographics[0]?.eduNumerator}</td>
                  <td>{demographics[0]?.eduDenominator}</td>
                  <td>{demographics[0]?.eduVariance}</td>
                  <td style={{ backgroundColor: demographics[0]?.eduPerformance >= 95 ? 'green' : demographics[0]?.eduPerformance >= 90 ? 'yellow' : 'red', color: demographics[0]?.eduPerformance >= 95 ? 'white' : demographics[0]?.eduPerformance >= 90 ? 'black' : 'white',
                   textAlign: 'center', fontWeight: 'bold' }}>
                    {demographics[0]?.eduPerformance} %
                   </td>

                  <td>
                    <div>
                    <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                    onClick={() => viewDetail("Proportion of all active patients with a documented educational Status", "patientDemo4" )}
                   >View</Button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">6</th>
                  <td>
                    Proportion of all active patients with a documented marital status
                  </td>
                  <td>{demographics[0]?.maritalNumerator}</td>
                  <td>{demographics[0]?.maritalDenominator}</td>
                  <td>{demographics[0]?.maritalVariance}</td>
                  <td style={{ backgroundColor: demographics[0]?.maritalPerformance >= 95 ? 'green' : demographics[0]?.maritalPerformance >= 90 ? 'yellow' : 'red', color: demographics[0]?.maritalPerformance >= 95 ? 'white' : demographics[0]?.maritalPerformance >= 90 ? 'black' : 'white',
                   textAlign: 'center', fontWeight: 'bold' }}>
                    {demographics[0]?.maritalPerformance} %
                  </td>

                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                    onClick={() => viewDetail("Proportion of all active patients with a documented marital", "patientDemo3" )}
                   >View</Button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">7</th>
                  <td>
                    Proportion of all active patients with documented occupational status
                  </td>
                  <td>{demographics[0]?.employNumerator}</td>
                  <td>{demographics[0]?.employDenominator}</td>
                   <td>{demographics[0]?.employVariance}</td>
                   <td style={{ backgroundColor: demographics[0]?.employPerformance >= 95 ? 'green' : demographics[0]?.employPerformance >= 90 ? 'yellow' : 'red', color: demographics[0]?.employPerformance >= 95 ? 'white' : demographics[0]?.employPerformance >= 90 ? 'black' : 'white',
                   textAlign: 'center', fontWeight: 'bold' }}>
                    {demographics[0]?.employPerformance} %
                   </td>

                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                    onClick={() => viewDetail("Proportion of all active patients with documented occupational status", "patientDemo5" )}
                   >View</Button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">8</th>
                  <td>
                    Proportion of all active patients with registered address/LGA of residence
                  </td>
                  <td>{demographics[0]?.addressNumerator}</td>
                  <td>{demographics[0]?.addressDenominator}</td>
                   <td>{demographics[0]?.addressVariance}</td>
                   <td style={{ backgroundColor: demographics[0]?.addressPerformance >= 95 ? 'green' : demographics[0]?.addressPerformance >= 90 ? 'yellow' : 'red', color: demographics[0]?.addressPerformance >= 95 ? 'white' : demographics[0]?.addressPerformance >= 90 ? 'black' : 'white',
                   textAlign: 'center', fontWeight: 'bold' }}>
                    {demographics[0]?.addressPerformance} %
                   </td>

                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                    onClick={() => viewDetail("Proportion of all active patients with registered address/LGA of residence", "patientDemo6" )}
                   >View</Button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </Table>
            </>)}
            {showPatientDetail &&(<>
                      <Button
                        variant="contained"
                        style={{backgroundColor:"#014d88", }}
                        className=" float-right mr-1"
                        //startIcon={<FaUserPlus />}
                        onClick={BackToList}
                        >
                        <span style={{ textTransform: "capitalize", color:"#fff" }}> {"<<"} Back </span>
                        </Button>
                        <br/>
                        <br/> 
                        <MaterialTable
                            icons={tableIcons}
                            title={getHeaderInfo}
                            columns={[

                              {
                                title: "Hospital Number",
                                field: "hospitalNumber",
                              },
                              { title: "Sex ", field: "sex", filtering: false },
                              { title: "Date Of Birth", field: "dob", filtering: false },
                              { title: "Status", field: "status", filtering: false },

                            ]}
                            data={ demographicsPatientsView.map((row) => ({
                              //Id: manager.id,
                              hospitalNumber: row.hospitalNumber,
                              sex: row.sex,
                              dob: row.dateOfBirth,
                              status:row.status

                            }))}

                            options={{
                              headerStyle: {
                                backgroundColor: "#014d88",
                                color: "#fff",
                              },
                              searchFieldStyle: {
                                width : '200%',
                                margingLeft: '250px',
                              },
                              filtering: false,
                              exportButton: true,
                              searchFieldAlignment: 'left',
                              pageSizeOptions:[10,20,100],
                              pageSize:10,
                              debounceInterval: 400
                            }}
                        />
            </>)}
          </div>
        </CardContent>
      </Card>
    </>
  );
};

export default DemographicsDQA;
