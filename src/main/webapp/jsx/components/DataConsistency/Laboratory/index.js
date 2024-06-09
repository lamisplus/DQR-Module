import React, { useEffect, useState } from "react";
import axios from "axios";
import MaterialTable from 'material-table';
import { token, url as baseUrl } from "../../../../api";
import { Form, Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";
import { Dropdown, Button, Menu, Icon } from "semantic-ui-react";

import ErrorIcon from "@mui/icons-material/Error";
import { FiUploadCloud } from "react-icons/fi";
import { forwardRef } from 'react';
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";
import CloudUpload from '@material-ui/icons/CloudUpload';
import moment from "moment";
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
        alignItems: "center"
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
        '& > *': {
            margin: theme.spacing(1)
        },
        "& .card-title":{
            color:'#fff',
            fontWeight:'bold'
        },
        "& .form-control":{
            borderRadius:'0.25rem',
            height:'41px'
        },
        "& .card-header:first-child": {
            borderRadius: "calc(0.25rem - 1px) calc(0.25rem - 1px) 0 0"
        },
        "& .dropdown-toggle::after": {
            display: " block !important"
        },
        "& select":{
            "-webkit-appearance": "listbox !important"
        },
        "& p":{
            color:'red'
        },
        "& label":{
            fontSize:'14px',
            color:'#014d88',
            fontWeight:'bold'
        }
    },
    demo: {
        backgroundColor: theme.palette.background.default,
    },
    inline: {
        display: "inline",
    },
    error:{
        color: '#f85032',
        fontSize: '12.8px'
    },
    success: {
        color: "#4BB543 ",
        fontSize: "11px",
    },
}));


    const Laboratory = (props) => {
    const classes = useStyles();
    const [laboratory, setLaboratory] = useState({});
    const [facilities, setFacilities] = useState([]);
    const [showPatientDetail, setPatientDetail] = useState(false);
    const [getHeaderInfo, setGetHeaderInfo] = useState("");
    const [laboratoryPatientsView, setLaboratoryPatientsView] = useState([])
    

    useEffect(() => {
        Facilities();
        loadLaboratory();
      }, []);
      const Facilities = () => {
      axios
        .get(`${baseUrl}account`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((response) => {
          setFacilities(response.data.currentOrganisationUnitId);
        })
        .catch((error) => {
          console.log(error);
        });
    };

    const loadLaboratory = () => {
      axios
        .get(`${baseUrl}dqr/laboratory-summary`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((response) => {
          setLaboratory(response.data);
          console.log(response.data)
        })
        .catch((error) => {
          console.log(error);
        });
    };
  
    const viewDetail =(headerTitle,patientDemoObj)=>{
      setPatientDetail(true)
      setGetHeaderInfo(headerTitle)
      const labDemo =patientDemoObj
      axios
            .get(`${baseUrl}dqr/patient-laboratory?indicator=${labDemo}`, {
              headers: { Authorization: `Bearer ${token}` },
            })
            .then((response) => {
              setLaboratoryPatientsView(response.data);
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
                    <h3>Laboratory</h3>
                    <div className="col-xl-12 col-lg-12">
                    {!showPatientDetail &&(<>
                        <Table bordered>
                            <thead>
                            <tr>
                                <th>
                                    #
                                </th>
                                <th>
                                    Complete Variables
                                </th>
                                <th>
                                    Numerator
                                </th>
                                <th>
                                  Denominator
                                </th>
                                <th> Variance </th>
                                <th>
                                  Performance
                                </th>
                                <th>
                                    Action
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <th scope="row">
                                    1
                                </th>
                                <td>
                                    Proportion of (active) eligible patients with documented viral load results done in the last one year
                                </td>
                                <td>{laboratory[0]?.eligibleVlNumerator}</td>
                                <td>{laboratory[0]?.eligibleVlDenominator}</td>
                                <td>{laboratory[0]?.eligibleVlVariance} </td>
                                <td style={{ backgroundColor: laboratory[0]?.eligibleVlPerformance >= 95 ? 'green' : laboratory[0]?.eligibleVlPerformance >= 90 ? 'yellow' : 'red', color: laboratory[0]?.eligibleVlPerformance >= 95 ? 'white' : laboratory[0]?.eligibleVlPerformance >= 90 ? 'black' : 'white',
                                    textAlign: 'center', fontWeight: 'bold' }}>{laboratory[0]?.eligibleVlPerformance} %</td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of (active) eligible patients with documented viral load results done in the last one year", "lab0" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    2
                                </th>
                                <td>
                                    Proportion of (active) eligible patients with viral load result that had documented collection date
                                </td>
                                <td>{laboratory[0]?.hadVlNumerator}</td>
                                <td>{laboratory[0]?.hadVlDenominator}</td>
                                <td>{laboratory[0]?.hadVlVariance} </td>
                                <td style={{ backgroundColor: laboratory[0]?.hadVlPerformance >= 95 ? 'green' : laboratory[0]?.hadVlPerformance >= 90 ? 'yellow' : 'red', color: laboratory[0]?.hadVlPerformance >= 95 ? 'white' : laboratory[0]?.hadVlPerformance >= 90 ? 'black' : 'white',
                                    textAlign: 'center', fontWeight: 'bold' }}>{laboratory[0]?.hadVlPerformance} %</td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of (active) eligible patients with viral load result that had documented collection date", "lab1" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    3
                                </th>
                                <td>
                                    Proportion of (active) eligible patients with viral load results with a documented date sample was received at the PCR Lab
                                </td>
                                <td>{laboratory[0]?.hadPcrDateNumerator}</td>
                                <td>{laboratory[0]?.hadPcrDateDenominator}</td>
                                <td>{laboratory[0]?.hadPcrDateVariance} </td>
                                <td style={{ backgroundColor: laboratory[0]?.hadPcrDatePerformance >= 95 ? 'green' : laboratory[0]?.hadPcrDatePerformance >= 90 ? 'yellow' : 'red', color: laboratory[0]?.hadPcrDatePerformance >= 95 ? 'white' : laboratory[0]?.hadPcrDatePerformance >= 90 ? 'black' : 'white',
                                    textAlign: 'center', fontWeight: 'bold' }}>{laboratory[0]?.hadPcrDatePerformance} %</td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of (active) eligible patients with viral load results with a documented date sample was received at the PCR Lab", "lab2" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    4
                                </th>
                                <td>
                                    Proportion of (active) eligible patients whose samples were collected with Viral load indication documented
                                </td>
                                <td>{laboratory[0]?.hadIndicatorNumerator}</td>
                                <td>{laboratory[0]?.hadIndicatorDenominator}</td>
                                <td>{laboratory[0]?.hadIndicatorVariance} </td>
                                <td style={{ backgroundColor: laboratory[0]?.hadIndicatorPerformance >= 95 ? 'green' : laboratory[0]?.hadIndicatorPerformance >= 90 ? 'yellow' : 'red', color: laboratory[0]?.hadIndicatorPerformance >= 95 ? 'white' : laboratory[0]?.hadIndicatorPerformance >= 90 ? 'black' : 'white',
                                    textAlign: 'center', fontWeight: 'bold' }}>{laboratory[0]?.hadIndicatorPerformance} %</td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of (active) eligible patients whose samples were collected with Viral load indication documented", "lab3" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    5
                                </th>
                                <td>
                                    Proportion of (active) eligible patients whose Date of VL result is after the date of VL sample collection
                                </td>
                                <td>{laboratory[0]?.vlDateGsDateNumerator}</td>
                                <td>{laboratory[0]?.vlDateGsDateDenominator}</td>
                                <td>{laboratory[0]?.vlDateGsDateVariance} </td>
                                <td style={{ backgroundColor: laboratory[0]?.vlDateGsDatePerformance >= 95 ? 'green' : laboratory[0]?.vlDateGsDatePerformance >= 90 ? 'yellow' : 'red', color: laboratory[0]?.vlDateGsDatePerformance >= 95 ? 'white' : laboratory[0]?.vlDateGsDatePerformance >= 90 ? 'black' : 'white',
                                    textAlign: 'center', fontWeight: 'bold' }}>{laboratory[0]?.vlDateGsDatePerformance} %</td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of (active) eligible patients whose samples were collected with Viral load indication documented", "lab4" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    6
                                </th>
                                <td>
                                    Proportion of (active) eligible patients with date of last CD4 in the last one year
                                </td>
                                <td>{laboratory[0]?.treatmentCd4Numerator}</td>
                                <td>{laboratory[0]?.treatmentCd4Denominator}</td>
                                <td>{laboratory[0]?.treatmentCd4Variance} </td>
                                <td style={{ backgroundColor: laboratory[0]?.treatmentCd4Performance >= 95 ? 'green' : laboratory[0]?.treatmentCd4Performance >= 90 ? 'yellow' : 'red', color: laboratory[0]?.treatmentCd4Performance >= 95 ? 'white' : laboratory[0]?.treatmentCd4Performance >= 90 ? 'black' : 'white',
                                    textAlign: 'center', fontWeight: 'bold' }}>{laboratory[0]?.treatmentCd4Performance} %</td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of (active) eligible patients with date of last CD4 in the last one year", "lab5" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    7
                                </th>
                                <td>
                                    Proportion of (active) eligible patients with CD4 result documented in the last one year
                                </td>
                                <td>{laboratory[0]?.cd4WithinYearNumerator}</td>
                                <td>{laboratory[0]?.cd4WithinYearDenominator}</td>
                                <td>{laboratory[0]?.cd4WithinYearVariance} </td>
                                <td style={{ backgroundColor: laboratory[0]?.cd4WithinYearPerformance >= 95 ? 'green' : laboratory[0]?.cd4WithinYearPerformance >= 90 ? 'yellow' : 'red', color: laboratory[0]?.cd4WithinYearPerformance >= 95 ? 'white' : laboratory[0]?.cd4WithinYearPerformance >= 90 ? 'black' : 'white',
                                    textAlign: 'center', fontWeight: 'bold' }}>{laboratory[0]?.cd4WithinYearPerformance} %</td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of (active) eligible patients with CD4 result documented in the last one year", "lab6" )}
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
                            data={ laboratoryPatientsView.map((row) => ({
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

export default Laboratory